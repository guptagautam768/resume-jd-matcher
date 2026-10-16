package dev.gautam.matcher.resume;

import dev.gautam.matcher.common.InvalidResumeFileException;
import dev.gautam.matcher.config.MatcherProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfIngestionService {

    private final ResumeRepository resumeRepository;
    private final VectorStore vectorStore;
    private final PdfValidator pdfValidator;
    private final CandidateNameExtractor nameExtractor;
    private final MatcherProperties properties;
    private final TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(500)
            .withMinChunkSizeChars(80)
            .withMinChunkLengthToEmbed(1)
            .withMaxNumChunks(200)
            .withKeepSeparator(true)
            .build();

    public PdfIngestionService(
            ResumeRepository resumeRepository,
            VectorStore vectorStore,
            PdfValidator pdfValidator,
            CandidateNameExtractor nameExtractor,
            MatcherProperties properties) {
        this.resumeRepository = resumeRepository;
        this.vectorStore = vectorStore;
        this.pdfValidator = pdfValidator;
        this.nameExtractor = nameExtractor;
        this.properties = properties;
    }

    public Resume ingestPdf(MultipartFile file) {
        pdfValidator.validate(file);
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new InvalidResumeFileException("Could not read the uploaded file");
        }
        String text = readPdf(bytes, file.getOriginalFilename());
        String fileName = file.getOriginalFilename() == null ? "resume.pdf" : file.getOriginalFilename();
        String candidateName = nameExtractor.fromText(text, fileName);
        return store(candidateName, fileName, text);
    }

    public Resume ingestText(String candidateName, String text) {
        if (candidateName == null || candidateName.isBlank()) {
            throw new InvalidResumeFileException("Candidate name is required");
        }
        if (candidateName.trim().length() > 120) {
            throw new InvalidResumeFileException("Candidate name must be 120 characters or fewer");
        }
        return store(candidateName.trim(), "pasted-text.txt", normalizeText(text));
    }

    private String readPdf(byte[] bytes, String filename) {
        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename == null ? "resume.pdf" : filename;
            }
        };
        try {
            List<Document> pages = new PagePdfDocumentReader(resource).get();
            StringBuilder builder = new StringBuilder();
            for (Document page : pages) {
                if (page.getText() != null) {
                    builder.append(page.getText()).append('\n');
                }
            }
            return normalizeText(builder.toString());
        } catch (RuntimeException ex) {
            throw new InvalidResumeFileException("Could not parse the PDF");
        }
    }

    private String normalizeText(String text) {
        String cleaned = text == null ? "" : text.replace("\u0000", "");
        cleaned = Arrays.stream(cleaned.split("\\R"))
                .map(line -> line.replaceAll("[ \\t]+", " ").trim())
                .collect(Collectors.joining("\n"))
                .trim();
        if (cleaned.isEmpty()) {
            throw new InvalidResumeFileException("Resume contained no extractable text");
        }
        if (cleaned.length() > properties.getMaxResumeTextChars()) {
            throw new InvalidResumeFileException(
                    "Resume text exceeds " + properties.getMaxResumeTextChars() + " characters");
        }
        return cleaned;
    }

    private Resume store(String candidateName, String fileName, String text) {
        Resume resume = resumeRepository.save(new Resume(UUID.randomUUID(), candidateName, fileName, text));
        List<Document> chunks = chunk(resume);
        vectorStore.add(chunks);
        return resume;
    }

    private List<Document> chunk(Resume resume) {
        List<Document> splits = splitter.apply(List.of(new Document(resume.getRawText())));
        if (splits.isEmpty()) {
            splits = List.of(new Document(resume.getRawText()));
        }
        List<Document> chunks = new ArrayList<>();
        int index = 0;
        for (Document split : splits) {
            String content = split.getText() == null ? "" : split.getText().trim();
            if (content.isEmpty()) {
                continue;
            }
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("resumeId", resume.getId().toString());
            metadata.put("chunkIdx", index);
            metadata.put("candidateName", resume.getCandidateName());
            chunks.add(new Document(content, metadata));
            index++;
        }
        if (chunks.isEmpty()) {
            throw new InvalidResumeFileException("Resume text produced no chunks to embed");
        }
        return chunks;
    }
}
