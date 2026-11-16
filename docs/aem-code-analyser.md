# AEM Code Analyser on this project

Everything below was run against this repository with the released CLI (`aem-analyser 1.0.10`, Java 21).
`scripts/aem-analyser-demo.sh` reproduces the whole matrix and asserts the exit code of every
invocation.

```bash
scripts/aem-analyser-demo.sh                    # every flag except the coverage runs
DEMO_COVERAGE=1 scripts/aem-analyser-demo.sh    # adds the JaCoCo / coverage flags
```

The analyser reasons across the whole reactor — `core`, `ui.apps`, `ui.config`, `ui.content`,
`dispatcher` — which is why it runs once at the root instead of once per module.

## What it reports here

```
Total Violations: 5 (0 Errors, 5 Warnings, 0 Infos)
```

11 modules, in about 5 seconds.

| Rule | Location | Issue |
|---|---|---|
| `AEM-SEC-001` | `ui.apps/…/components/xfpage/customfooterlibs.html:25` | `@ context='unsafe'` disables XSS escaping |
| `AEM-SEC-001` | `ui.apps/…/components/xfpage/customheaderlibs.html:29` | Same expression in the header clientlibs |
| `java:S2208` | `it.tests/…/GetPageIT.java:24` | Wildcard import |
| `java:S1166` | `it.tests/…/PublishPageValidationIT.java:87` | Exception swallowed without logging |
| `java:S1155` | `it.tests/…/PublishPageValidationIT.java:114` | `size() == 0` instead of `isEmpty()` |

Two are AEM-specific (`AEM-SEC-001`, an escaping bypass no Java-only analyser sees); three come from
the embedded SonarJava analysis, which is on by default. With `--no-sonar` the project reports **2**.

## How the root is found

| Invocation | Analysed root | Result here |
|---|---|---|
| `aem-analyser` (no arguments) | the current directory | 5 findings |
| `aem-analyser .` | the positional path | 5 findings |
| `aem-analyser -p .` / `--project-root .` | the given path | 5 findings |
| run inside `core/` | the reactor that owns it | the same reactor, not just `core` |

## Report formats

| Flag | File written |
|---|---|
| `-f CONSOLE` (default) | terminal report |
| `-f SARIF` | `aem-analyser.sarif` — GitHub code scanning, Azure DevOps |
| `-f HTML` | `aem-analyser-report.html` — the dashboard |
| `-f SONAR` | `sonar-aem-analyser-report.json` — `sonar.externalIssuesReportPaths` |
| `-f GITHUB` | `github-annotations.txt` — `::error file=…,line=…` workflow commands |
| `-f GITLAB` | `gitlab-code-quality.json` — GitLab Code Quality widget |
| `-f AZURE` | `azure-pipelines.log` — Azure logging commands |
| `-f SARIF,SONAR,GITLAB,AZURE,GITHUB` | all of them into one `-o` directory |
| `--no-html` / `--html <path>` | skip, or place, the dashboard |
| `-o, --output <dir>` | where the files go |

## Quality gates

| Flag | Exit | Why |
|---|---|---|
| `--fail-on ERROR` | **0** | no errors on this project |
| `--fail-on WARNING` | **1** | five warnings — the setting that would gate the build |
| `--fail-on-warning` | **1** | legacy alias for the same |
| `--fail-on NONE` | **0** | report-only: findings printed, exit forced to 0 |
| `--max-warnings 0` | **1** | 5 > 0 |
| `--max-warnings 20` | **0** | 5 ≤ 20 |
| `--max-duplication 1` | **0** | duplication below 1 % |

Exit codes overall: `0` clean or below threshold, `1` findings at or above the threshold, `2` the
analysis itself failed.

## Scoping a run

| Flag | Observed |
|---|---|
| `--changed-only` | 0 findings (working tree clean) |
| `--since origin/main` | 0 findings — nothing differs from `origin/main` on this branch; the tool prints `Git-diff filtering active (N changed file(s) found)` |
| `--max-file-size 1000` | 3 findings — files larger than 1 000 bytes are skipped |
| `--ignore-config "ui.config/**/config.publish/**"` | 5 findings — no `config.publish` configuration exists here to exclude |
| `--ignored-services <class>` | 5 findings — nothing is flagged as an unused service, so nothing to suppress |
| `--dependency-jars ~/.m2/repository/org/apache/sling` | 5 findings — no PID findings come from those bundles |

## Rule configuration

```yaml
# .aem-analyser.yml
schemaVersion: 1
rules:
  java:S1166:
    enabled: false      # ignore swallowed exceptions
  AEM-SEC-001:
    severity: ERROR     # promote to a blocking finding
```

With that file, `--config .aem-analyser.yml` reports **4 findings (2 errors, 2 warnings) and exits 1**:
the `java:S1166` finding disappears and both `AEM-SEC-001` findings become errors. Severity changes
flow into the reports *and* the exit code.

## Baselines

```bash
aem-analyser --update-baseline                                          # writes .aem-analyser-baseline.json
aem-analyser --baseline .aem-analyser-baseline.json                     # 0 findings, exit 0
aem-analyser --baseline .aem-analyser-baseline.json --baseline-report   # 5 findings, all at INFO
```

That is how a project adopts the tool with existing debt: freeze today, fail only on new findings.

## Remediation helpers

| Flag | Observed |
|---|---|
| `--fix-dry-run` | exit 0, writes nothing |
| `--fix` | `No auto-fixable code smells detected in target files.` — none of the findings here has a safe automatic fix, and no source file was modified |

## Coverage gates

```bash
aem-analyser --project-root core --collect-coverage              # runs the module's tests
aem-analyser --project-root core --collect-coverage --force      # re-run even if coverage is fresh
aem-analyser --project-root core --collect-coverage --min-coverage 10
```

Observed: the core module's tests run, `Test Code Coverage: 51.9% Lines`, and the gate prints
`Line Coverage: 51.9% (Threshold: >= 10.0%) [PASS]`. Without `--force` a second run notices the
existing `jacoco.exec` and skips test execution.

## Exploring the tool

| Flag | Output |
|---|---|
| `--explain AEM-SEC-001` | the rule's severity, remediation and rationale in the terminal |
| `--list-rules` | the full catalogue with severities and effort estimates |
| `--generate-rule-docs <dir>` | 31 Markdown pages (30 rules + index) |
| `-v, --verbose` | parser and scan diagnostics — which files were skipped, and why |
| `--history <dir>` | appends to `runs.jsonl` and renders the trend in the dashboard |
| `--history-limit 5` | how many runs the trend shows |

## Analysis knobs

| Flag | Observed |
|---|---|
| `--sonar` (default) | 5 findings |
| `--no-sonar` | 2 findings — AEM rules only |
| `--sonar-rules default` | the embedded ruleset |
| `-t 1` / `-t 8` | same 5 findings, different parse parallelism |
| `--profile production` | same 5 findings; profiles only affect module discovery |
| `--timeout 60` | completes well inside the budget — a full run takes ~5 s |
| `--no-color` | plain text, for CI logs |

## CLI flags and Maven parameters

Every option exists in both front ends except for a few that only make sense in one of them. The Maven
plugin takes them as `<configuration>` elements or as `-Daem-analyser.<name>=<value>` properties.

| CLI | Maven plugin | Notes |
|---|---|---|
| `[project-root]`, `-p` | `projectRoot` | defaults to the reactor root |
| `-f, --format` | `formats` / `format` | same format names |
| `--html`, `--no-html` | `generateHtml` | |
| `-o, --output` | `outputFile` | file for one format, directory for several |
| `--fail-on` | `failOn` | `check` goal only |
| `--fail-on-warning` | `failOnWarning` | `check` goal only |
| `--baseline`, `--update-baseline`, `--baseline-report` | `baseline`, `updateBaseline`, `baselineReport` | |
| `--config` | `configFile` | `.aem-analyser.yml` |
| `--ignore-config` | `ignoredConfigPaths` | |
| `--ignored-services` | `ignoredServices` | |
| `--dependency-jars` | `dependencyJars` | |
| `--changed-only`, `--since` | `changedOnly`, `since` | |
| `--fix`, `--fix-dry-run` | `fix`, `fixDryRun` | |
| `--min-coverage`, `--max-duplication`, `--max-warnings` | `minCoverage`, `maxDuplication`, `maxWarnings` | |
| `--sonar` / `--no-sonar` | `enableSonar` | |
| `--sonar-rules` | `sonarRules` | |
| `--history`, `--history-limit` | `historyDirectory`, `historyLimit` | the element is named after the field, not the property | |
| `-t, --threads` | `threads` | |
| `--profile` | `profiles` | |
| `--max-file-size` | `maxFileSizeBytes` / `maxFileSize` | |
| `--timeout` | `timeoutSeconds` / `timeout` | |
| `-v, --verbose` | `verbose` | |
| — | `skip` | plugin only: turn the execution off (`-Daem-analyser.skip=true`) |
| `--collect-coverage`, `--force` | — | CLI only: the CLI runs the tests itself; inside a build the coverage comes from the build's own JaCoCo run |
| `--explain`, `--list-rules`, `--generate-rule-docs` | — | CLI only: catalogue inspection, not analysis |
| `--no-color` | — | CLI only: Maven controls terminal colour |

Example of the same run through the plugin:

```bash
mvn -B -ntp io.github.rohithv16:aem-analyser-maven-plugin:1.0.10:report \
  -Daem-analyser.formats=SARIF,HTML \
  -Daem-analyser.enableSonar=false \
  -Daem-analyser.failOn=WARNING
```

## Why it is worth running in CI

`AEM-SEC-001` on the two `xfpage` clientlibs is exactly the kind of finding that survives review: an
HTL expression that switches off escaping is invisible both to a Java analyser and to a per-module
scan. `.github/workflows/static-analysis.yml` uploads the SARIF on every pull request, so it surfaces
as a code-scanning annotation on the diff.
