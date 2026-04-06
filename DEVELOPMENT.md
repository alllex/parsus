# Development

## Publishing locally

Publish all artifacts to the local Maven repository (`~/.m2/repository/me/alllex/parsus/`):

```shell
./gradlew publishToMavenLocal
```

To publish a specific target only:

```shell
./gradlew publishJvmPublicationToMavenLocal
```

The version is read from `version.txt` (currently a `-SNAPSHOT`).
To test with a specific version locally, edit `version.txt` before publishing.

## Releasing to Maven Central

### Prerequisites

The following secrets must be configured in GitHub Actions:

| Secret              | Description                    |
|---------------------|--------------------------------|
| `SIGNING_KEY_ID`    | GPG key ID                     |
| `SIGNING_KEY`       | GPG private key (PEM format)   |
| `SIGNING_PASSWORD`  | GPG key passphrase             |
| `SONATYPE_USERNAME` | Sonatype/Maven Central username |
| `SONATYPE_PASSWORD` | Sonatype/Maven Central password |

### Release steps

1. Update the version in `version.txt` (remove `-SNAPSHOT` suffix).
2. Commit and push to `main`.
3. Create and push a tag:
   ```shell
   git tag v<version>
   git push origin v<version>
   ```
4. The [Release workflow](.github/workflows/release.yml) triggers automatically on `v*` tags.
   It publishes from three OS runners in parallel:
   - **macOS** — shared metadata, JVM, JS, linuxArm64, and kotlinMultiplatform artifacts
   - **Ubuntu** — linuxX64 native artifact
   - **Windows** — mingwX64 native artifact
5. After the workflow completes, verify the artifacts on Maven Central.
6. Update `version.txt` to the next `-SNAPSHOT` version and push.

### Manual release

The release workflow can also be triggered manually via `workflow_dispatch` from the GitHub Actions UI.
