#!/usr/bin/env python3
"""Write the synthetic sample resume PDFs. Standard library only. ASCII text."""

from pathlib import Path

ROOT = Path(__file__).resolve().parent / "resumes"

RESUMES = {
    "01-priya-natarajan.pdf": [
        "Priya Natarajan",
        "SYNTHETIC SAMPLE - fictional candidate, not a real person",
        "Senior Backend Engineer",
        "",
        "Summary",
        "Backend engineer who builds Java services, relational schemas, and cloud deploys.",
        "This biography is invented for a portfolio demo.",
        "",
        "Experience",
        "Senior Backend Engineer, Northwind Labs (fictional), 2021-2026",
        "- Built onboarding APIs with Java 21 and Spring Boot.",
        "- Designed PostgreSQL schemas and Flyway migrations for billing data.",
        "- Ran services on AWS with Docker images, ECS, and RDS.",
        "- Added Kafka consumers for invoice events and JUnit test suites.",
        "- Exposed REST APIs used by internal web apps.",
        "Backend Engineer, Harbor and Co (fictional), 2017-2021",
        "- Moved a billing module into Spring Boot microservices.",
        "- Tuned PostgreSQL queries and added reporting indexes.",
        "- Introduced Docker for local integration tests.",
        "",
        "Skills",
        "Java, Spring Boot, Spring Data JPA, Hibernate, PostgreSQL, REST,",
        "Docker, AWS, Kafka, JUnit, Flyway, Git",
        "",
        "Education",
        "B.S. Computer Science, Redwood State University (fictional), 2017",
    ],
    "02-diego-alvarez.pdf": [
        "Diego Alvarez",
        "SYNTHETIC SAMPLE - fictional candidate, not a real person",
        "Frontend Engineer",
        "",
        "Summary",
        "Interface engineer focused on component design, accessibility, and typed UI code.",
        "This biography is invented for a portfolio demo.",
        "",
        "Experience",
        "Frontend Engineer, Lumen Street Studio (fictional), 2020-2026",
        "- Shipped a design system in React and TypeScript.",
        "- Built Next.js marketing pages and CSS layout systems.",
        "- Wrote Jest and Testing Library coverage for shared components.",
        "- Partnered with content editors on accessible forms.",
        "UI Engineer, Paper Lantern (fictional), 2018-2020",
        "- Rebuilt a dashboard with React hooks and TypeScript.",
        "- Improved keyboard navigation and color contrast.",
        "",
        "Skills",
        "React, TypeScript, JavaScript, Next.js, CSS, Jest, accessibility, Figma",
        "",
        "Education",
        "B.A. Design and Computing, Mesa College (fictional), 2018",
    ],
    "03-amira-hassan.pdf": [
        "Amira Hassan",
        "SYNTHETIC SAMPLE - fictional candidate, not a real person",
        "Data Engineer",
        "",
        "Summary",
        "Data engineer who models warehouses and schedules batch pipelines.",
        "This biography is invented for a portfolio demo.",
        "",
        "Experience",
        "Data Engineer, Bluebird Analytics (fictional), 2019-2026",
        "- Built Spark jobs that land billing events in a warehouse.",
        "- Scheduled pipelines with Airflow and published dbt models.",
        "- Wrote SQL transformations for finance and product analysts.",
        "- Owned AWS Glue crawlers and S3 dataset layout.",
        "Analytics Engineer, Fieldnote (fictional), 2016-2019",
        "- Maintained nightly Python extract jobs.",
        "- Documented metric definitions for the warehouse.",
        "",
        "Skills",
        "Python, Spark, Airflow, SQL, dbt, AWS, data modeling",
        "",
        "Education",
        "M.S. Information Systems, Lakeshore Institute (fictional), 2016",
    ],
    "04-jonah-blake.pdf": [
        "Jonah Blake",
        "SYNTHETIC SAMPLE - fictional candidate, not a real person",
        "Junior Java Developer",
        "",
        "Summary",
        "Early-career Java developer with one internship and a small set of services.",
        "This biography is invented for a portfolio demo.",
        "",
        "Experience",
        "Junior Developer, Cedarstack (fictional), 2024-2026",
        "- Fixed bugs in a Spring Boot internal tool.",
        "- Added REST endpoints backed by MySQL.",
        "- Wrote a handful of JUnit tests for form validation.",
        "Intern, Cedarstack (fictional), 2023",
        "- Paired on Java classes for a course catalog.",
        "- Learned Git, code review, and basic SQL.",
        "",
        "Skills",
        "Java, Spring Boot, MySQL, REST, JUnit, Git",
        "",
        "Education",
        "B.S. Computer Science, Cedar State (fictional), 2024",
    ],
    "05-mei-chen.pdf": [
        "Mei Chen",
        "SYNTHETIC SAMPLE - fictional candidate, not a real person",
        "Site Reliability Engineer",
        "",
        "Summary",
        "Platform engineer who runs clusters, infrastructure code, and on-call.",
        "This biography is invented for a portfolio demo.",
        "",
        "Experience",
        "SRE, Relay Cloud (fictional), 2020-2026",
        "- Managed Kubernetes clusters and Terraform modules.",
        "- Built deployment tooling in Go.",
        "- Ran Prometheus alerts and incident reviews.",
        "- Touched a small Java admin utility, not the main product.",
        "Systems Engineer, North Pier Hosting (fictional), 2016-2020",
        "- Automated VM provisioning and Linux patching.",
        "- Wrote Docker images for internal agents.",
        "",
        "Skills",
        "Kubernetes, Terraform, Go, Docker, Prometheus, Linux, AWS",
        "",
        "Education",
        "B.S. Information Technology, Harbor Polytechnic (fictional), 2016",
    ],
    "06-samir-okonkwo.pdf": [
        "Samir Okonkwo",
        "SYNTHETIC SAMPLE - fictional candidate, not a real person",
        "Full-Stack Engineer",
        "",
        "Summary",
        "Product engineer who ships Spring Boot APIs and React screens.",
        "This biography is invented for a portfolio demo.",
        "",
        "Experience",
        "Full-Stack Engineer, Paperkite (fictional), 2019-2026",
        "- Built order APIs with Java and Spring Boot.",
        "- Stored orders in PostgreSQL and shipped schema changes with Flyway.",
        "- Deployed Docker images to AWS Elastic Beanstalk.",
        "- Wrote the companion React and TypeScript order screen.",
        "- Covered services with JUnit and REST contract checks.",
        "Software Engineer, Kindling Apps (fictional), 2016-2019",
        "- Maintained a Hibernate data layer and REST controllers.",
        "",
        "Skills",
        "Java, Spring Boot, Hibernate, PostgreSQL, Flyway, REST,",
        "Docker, AWS, React, TypeScript, JUnit",
        "",
        "Education",
        "B.S. Software Engineering, Ironwood University (fictional), 2016",
    ],
    "07-elena-petrova.pdf": [
        "Elena Petrova",
        "SYNTHETIC SAMPLE - fictional candidate, not a real person",
        "Product Manager",
        "",
        "Summary",
        "Product manager who writes roadmaps, specs, and release notes.",
        "This biography is invented for a portfolio demo.",
        "",
        "Experience",
        "Product Manager, Fieldnote (fictional), 2018-2026",
        "- Wrote problem statements and sequenced a quarterly roadmap.",
        "- Interviewed customers and summarized feedback for engineers.",
        "- Tracked launch checklists and stakeholder updates.",
        "- Learned enough SQL to read a dashboard, not to design schemas.",
        "Associate PM, Northwind Labs (fictional), 2015-2018",
        "- Coordinated beta programs and help-center articles.",
        "",
        "Skills",
        "Roadmapping, writing, stakeholder updates, SQL basics, Jira",
        "",
        "Education",
        "B.A. English, Westbridge College (fictional), 2015",
    ],
    "08-noah-williams.pdf": [
        "Noah Williams",
        "SYNTHETIC SAMPLE - fictional candidate, not a real person",
        "Machine Learning Engineer",
        "",
        "Summary",
        "ML engineer who trains models and serves them behind small Python APIs.",
        "This biography is invented for a portfolio demo.",
        "",
        "Experience",
        "ML Engineer, Kindling ML (fictional), 2020-2026",
        "- Trained ranking models with Python and PyTorch.",
        "- Served features through a FastAPI service.",
        "- Built offline evaluation notebooks for product science.",
        "- Stored experiment rows in PostgreSQL.",
        "Research Assistant, Redwood State University (fictional), 2018-2020",
        "- Reproduced papers and cleaned public datasets.",
        "",
        "Skills",
        "Python, PyTorch, FastAPI, PostgreSQL, pandas, experiment tracking",
        "",
        "Education",
        "M.S. Computer Science, Redwood State University (fictional), 2020",
    ],
}


def escape(text: str) -> str:
    if not text.isascii():
        raise SystemExit(f"Non-ASCII text is not allowed: {text!r}")
    return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")


def content_stream(lines: list[str]) -> bytes:
    parts = ["BT", "/F1 11 Tf", "54 760 Td", "14 TL"]
    for index, line in enumerate(lines):
        if index:
            parts.append("T*")
        parts.append(f"({escape(line)}) Tj")
    parts.append("ET")
    return "\n".join(parts).encode("ascii")


def build_pdf(lines: list[str]) -> bytes:
    if len(lines) > 48:
        raise SystemExit(f"Too many lines for one page: {len(lines)}")
    stream = content_stream(lines)
    objects = {
        1: b"<< /Type /Catalog /Pages 2 0 R >>",
        2: b"<< /Type /Pages /Kids [5 0 R] /Count 1 >>",
        3: b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
        4: f"<< /Length {len(stream)} >>\nstream\n".encode("ascii") + stream + b"\nendstream",
        5: (
            b"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
            b"/Contents 4 0 R /Resources << /Font << /F1 3 0 R >> >> >>"
        ),
    }
    output = bytearray(b"%PDF-1.4\n")
    offsets = {}
    for number in range(1, 6):
        offsets[number] = len(output)
        output.extend(f"{number} 0 obj\n".encode("ascii"))
        output.extend(objects[number])
        output.extend(b"\nendobj\n")
    xref = len(output)
    output.extend(b"xref\n0 6\n")
    output.extend(b"0000000000 65535 f \n")
    for number in range(1, 6):
        output.extend(f"{offsets[number]:010d} 00000 n \n".encode("ascii"))
    output.extend(
        f"trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n{xref}\n%%EOF\n".encode("ascii")
    )
    return bytes(output)


def main() -> None:
    ROOT.mkdir(parents=True, exist_ok=True)
    for name, lines in RESUMES.items():
        path = ROOT / name
        path.write_bytes(build_pdf(lines))
        print(f"wrote {path} ({len(lines)} lines)")


if __name__ == "__main__":
    main()
