# Deploying Resume & JD Matcher on AWS

This guide deploys the container from the repo `Dockerfile` to Amazon ECR, then runs it on App Runner or ECS Fargate against RDS for PostgreSQL. It does not claim that a live environment exists, and it does not include measured cost or latency numbers. Check the AWS pricing pages for the region and sizes you choose before you leave anything running.

The service has no authentication. Use it with the synthetic samples, or put it on a private network. Do not expose it to the internet with real resumes.

## What you need

- An AWS account and a region, written below as `$AWS_REGION`
- AWS CLI v2 and Docker
- The GitHub Actions OIDC role only if you want `.github/workflows/ecr.yml` to push the image
- An OpenAI API key, stored in AWS and never committed

## 1. RDS PostgreSQL with pgvector

Create a PostgreSQL 16 instance. pgvector is available as an extension on RDS PostgreSQL; you still have to create it in the application database.

Keep the instance private. Its security group should allow port 5432 only from the App Runner VPC connector security group or the ECS task security group.

After the instance is available, connect as the master user and run:

```sql
CREATE DATABASE matcher;
\c matcher
CREATE EXTENSION IF NOT EXISTS vector;
```

Flyway also runs `CREATE EXTENSION IF NOT EXISTS vector` on startup. The database user in `SPRING_DATASOURCE_USERNAME` needs permission to create the extension, or you create it once as the master user before the app boots. Flyway then creates `resume`, `job_description`, and `match_result`. Spring AI creates `vector_store` with a 1536-dimension embedding column for `text-embedding-3-small`.

Store the database URL, username, and password in Secrets Manager as a JSON secret, for example `resume-jd-matcher/db`:

```json
{
  "url": "jdbc:postgresql://<rds-endpoint>:5432/matcher",
  "username": "matcher_app",
  "password": "<generated-password>"
}
```

Use a dedicated database user, not the RDS master user, for the application.

## 2. OpenAI key

Create a second secret, `resume-jd-matcher/openai`, with the plaintext key or a JSON field named `OPENAI_API_KEY`. Do not put the key in the task definition in plain text, in the image, or in git.

## 3. Image in ECR

Create a repository:

```bash
aws ecr create-repository --repository-name resume-jd-matcher --region "$AWS_REGION"
```

Build and push from a machine that has the repo checked out:

```bash
ACCOUNT=$(aws sts get-caller-identity --query Account --output text)
REGISTRY="$ACCOUNT.dkr.ecr.$AWS_REGION.amazonaws.com"
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$REGISTRY"
docker build -t "$REGISTRY/resume-jd-matcher:latest" .
docker push "$REGISTRY/resume-jd-matcher:latest"
```

The image listens on port 8080 and starts with `java -jar`. It expects:

| Variable | Purpose |
|---|---|
| `OPENAI_API_KEY` | OpenAI credential |
| `SPRING_DATASOURCE_URL` | JDBC URL for the RDS database |
| `SPRING_DATASOURCE_USERNAME` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `OPENAI_CHAT_MODEL` | Optional. Default `gpt-4o-mini` |
| `OPENAI_EMBEDDING_MODEL` | Optional. Default `text-embedding-3-small` |

Changing the embedding model or `matcher.embedding-dimensions` requires a new `vector_store` table. The default column is `vector(1536)`.

### GitHub Actions

`.github/workflows/ci.yml` runs `./mvnw verify` and `docker build`. It does not push.

`.github/workflows/ecr.yml` is `workflow_dispatch` only. Configure GitHub OIDC, then set:

- secret `AWS_ROLE_ARN`
- variable `AWS_REGION`
- variable `ECR_REPOSITORY` (`resume-jd-matcher`)

The role needs `ecr:GetAuthorizationToken` plus push permissions on that repository. Prefer OIDC over long-lived access keys.

## 4. App Runner

App Runner is the smaller path.

1. Create an App Runner service from the private ECR image.
2. Port `8080`.
3. Health check: HTTP `GET /actuator/health`. A successful response has `"status":"UP"`.
4. Inject the environment variables above. Map `OPENAI_API_KEY` and the database password from Secrets Manager rather than typing them into the console and leaving them in shell history.
5. Attach a VPC connector whose subnets can reach the RDS instance, and allow the connector's security group on the database security group.

App Runner's default egress is public, which the OpenAI API needs, while the VPC connector is what reaches a private RDS instance. Confirm both paths in the App Runner networking docs for the service mode you pick. If the connector's egress is VPC-only, OpenAI calls need a NAT gateway.

After the service is healthy:

```bash
curl -f "https://<app-runner-host>/actuator/health"
```

Then upload a file from `samples/resumes/` and create a job from `samples/jobs/`. Those files are fictional.

## 5. Alternative: ECS Fargate and an ALB

Use this when you want a private subnet layout or an existing cluster.

1. Cluster with Fargate.
2. Task definition: the ECR image, 8080, the same environment variables, secrets from Secrets Manager (`valueFrom` on the secret ARN).
3. Task role: no AWS API calls are required by the app itself. The execution role needs ECR pull and `secretsmanager:GetSecretValue` for the two secrets.
4. Service in private subnets, behind an internet-facing or internal ALB.
5. Target group health check path `/actuator/health`, matcher `200`.
6. Security groups: ALB to tasks on 8080, tasks to RDS on 5432, tasks out to the internet through NAT for `api.openai.com`.

The ALB domain is the base URL. The same curl checks as App Runner apply. MCP clients use `https://<alb-host>/sse`.

## 6. Smoke test

```bash
BASE=https://<host>
curl -f "$BASE/actuator/health"
curl -F "files=@samples/resumes/01-priya-natarajan.pdf" "$BASE/api/resumes"
curl -H "Content-Type: application/json" -d @samples/jobs/senior-backend-engineer.json "$BASE/api/jobs"
curl -X POST "$BASE/api/jobs/<job-id>/match?topK=3"
```

A match calls OpenAI. A failure with HTTP 502 means the model response could not be parsed. A startup failure about the API key means `OPENAI_API_KEY` was empty.

## 7. Tear down

Delete the App Runner service or ECS service and ALB, delete the ECR images if you do not need them, delete the secrets, and delete the RDS instance if you created it only for this demo. RDS snapshots and NAT gateways are the items that keep billing after the app is gone. Confirm in the billing console; this document does not estimate a monthly cost.
