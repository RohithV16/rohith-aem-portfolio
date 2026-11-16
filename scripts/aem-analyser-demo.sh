#!/usr/bin/env bash
# Demonstrates every AEM Code Analyser flag against this project.
#
#   scripts/aem-analyser-demo.sh              # everything except the coverage runs
#   DEMO_COVERAGE=1 scripts/aem-analyser-demo.sh   # also runs the JaCoCo/coverage flags
#
# Nothing here modifies the repository: --update-baseline and --fix run against a temporary
# copy, and --fix-dry-run / --generate-rule-docs write into a temp directory.
set -uo pipefail

JAR="${AEM_ANALYSER_JAR:-}"
if [ -z "$JAR" ]; then
  JAR=$(find "$HOME/.m2/repository/io/github/rohithv16/aem-analyser-cli" -name 'aem-analyser-cli-*.jar' \
        ! -name '*-sources.jar' ! -name '*-javadoc.jar' 2>/dev/null | sort -V | tail -1)
fi
if [ -z "$JAR" ] || [ ! -f "$JAR" ]; then
  echo "No analyser jar found. Download aem-analyser-cli.jar from the releases page and set" >&2
  echo "AEM_ANALYSER_JAR=/path/to/aem-analyser-cli.jar (or install aem-analyser-cli via Maven)." >&2
  exit 2
fi

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

fail=0
show() { # show <label> <expected-exit> <args...>
  local label="$1" expected="$2"; shift 2
  local log="$WORK/out.log"
  java -jar "$JAR" "$@" > "$log" 2>&1
  local code=$?
  local total
  total=$(grep -oE "Total Violations: [0-9]+ \([^)]*\)" "$log" | head -1)
  local verdict="ok"
  if [ "$code" != "$expected" ]; then verdict="UNEXPECTED EXIT"; fail=1; fi
  printf '%-42s exit=%s (%s)  %s\n' "$label" "$code" "$verdict" "${total:-$(grep -m1 -oE 'GATE (PASSED|FAILED)[^.]*' "$log")}"
}

echo "jar: $JAR"
java -jar "$JAR" --version

echo
echo "== how the root is found =="
show "no arguments (current directory)" 0 --no-html --no-color
show "positional root: ." 0 . --no-html --no-color
show "explicit -p ." 0 -p . --no-html --no-color

echo
echo "== report formats =="
show "-f CONSOLE" 0 -f CONSOLE --no-html --no-color
show "-f SARIF" 0 -f SARIF -o "$WORK/sarif" --no-html --no-color
show "-f HTML" 0 -f HTML -o "$WORK/html" --no-color
show "-f SONAR" 0 -f SONAR -o "$WORK/sonar" --no-html --no-color
show "-f GITHUB" 0 -f GITHUB -o "$WORK/gh" --no-html --no-color
show "-f GITLAB" 0 -f GITLAB -o "$WORK/gl" --no-html --no-color
show "-f AZURE" 0 -f AZURE -o "$WORK/az" --no-html --no-color
show "all formats at once" 0 -f SARIF,SONAR,GITLAB,AZURE,GITHUB -o "$WORK/all" --no-html --no-color
echo "  wrote: $(ls "$WORK/all" | tr '\n' ' ')"

echo
echo "== quality gates =="
show "--fail-on ERROR (this project has none)" 0 --no-html --no-color --fail-on ERROR
show "--fail-on WARNING" 1 --no-html --no-color --fail-on WARNING
show "--fail-on-warning (alias)" 1 --no-html --no-color --fail-on-warning
show "--fail-on NONE (report only)" 0 --no-html --no-color --fail-on NONE
show "--max-warnings 0" 1 --no-html --no-color --max-warnings 0
show "--max-warnings 20" 0 --no-html --no-color --max-warnings 20
show "--max-duplication 1" 0 --no-html --no-color --max-duplication 1

echo
echo "== scoping =="
show "--changed-only (clean tree)" 0 --no-html --no-color --changed-only
show "--since origin/main" 0 --no-html --no-color --since origin/main
show "--max-file-size 1000 (skips larger files)" 0 --no-html --no-color --max-file-size 1000
show "--ignore-config config.publish" 0 --no-html --no-color --ignore-config "ui.config/**/config.publish/**"
show "--ignored-services <class>" 0 --no-html --no-color --ignored-services com.rohithaem.portfolio.core.commons.ResourceResolverService
show "--dependency-jars <dir>" 0 --no-html --no-color --dependency-jars "$HOME/.m2/repository/org/apache/sling"

echo
echo "== configuration file =="
cat > "$WORK/policy.yml" <<'YAML'
schemaVersion: 1
rules:
  java:S1166:
    enabled: false
  AEM-SEC-001:
    severity: ERROR
YAML
show "--config <rules + allowlists> (SEC-001 is ERROR here)" 1 --no-html --no-color --config "$WORK/policy.yml"

echo
echo "== baseline (against a throwaway copy) =="
COPY="$WORK/copy"
rsync -a --exclude target --exclude node_modules --exclude .git "$ROOT/" "$COPY/"
( cd "$COPY" && java -jar "$JAR" --no-html --no-color --update-baseline >/dev/null 2>&1
  printf '%-42s exit=0  baseline: %s bytes\n' "--update-baseline" "$(wc -c < .aem-analyser-baseline.json | tr -d ' ')"
  java -jar "$JAR" --no-html --no-color --baseline .aem-analyser-baseline.json > "$WORK/b.log" 2>&1
  printf '%-42s exit=%s  %s\n' "--baseline (gate only new findings)" "$?" "$(grep -oE 'Total Violations: [0-9]+ \([^)]*\)' "$WORK/b.log")"
  java -jar "$JAR" --no-html --no-color --baseline .aem-analyser-baseline.json --baseline-report > "$WORK/b2.log" 2>&1
  printf '%-42s exit=%s  %s\n' "--baseline-report (accepted shown as INFO)" "$?" "$(grep -oE 'Total Violations: [0-9]+ \([^)]*\)' "$WORK/b2.log")" )

echo
echo "== remediation helpers =="
show "--fix-dry-run (preview, writes nothing)" 0 --no-html --no-color --fix-dry-run
( cd "$COPY" && java -jar "$JAR" --no-html --no-color --fix > "$WORK/fix.log" 2>&1
  printf '%-42s exit=%s  %s\n' "--fix" "$?" "$(grep -m1 -iE 'auto-fixable|No auto-fixable' "$WORK/fix.log" | sed 's/^\[INFO\] //')" )

echo
echo "== discovery of the tool itself =="
show "--explain AEM-SEC-001" 0 --explain AEM-SEC-001
show "--list-rules" 0 --list-rules
show "--generate-rule-docs <dir>" 0 --generate-rule-docs "$WORK/rules"
echo "  rule pages generated: $(ls "$WORK/rules" | wc -l | tr -d ' ')"

echo
echo "== analysis detail =="
show "--sonar (default, embedded SonarJava)" 0 --no-html --no-color --sonar
show "--no-sonar (AEM rules only)" 0 --no-html --no-color --no-sonar
show "--sonar-rules default" 0 --no-html --no-color --sonar-rules default
show "-v/--verbose" 0 --no-html --no-color -v
show "--threads 1" 0 --no-html --no-color -t 1
show "--threads 8" 0 --no-html --no-color -t 8
show "--profile production" 0 --no-html --no-color --profile production
show "--timeout 60" 0 --no-html --no-color --timeout 60
show "--history <dir>" 0 --no-html --no-color --history "$WORK/history"
show "--history-limit 5" 0 --no-html --no-color --history "$WORK/history" --history-limit 5
echo "  history runs recorded: $(wc -l < "$WORK/history/runs.jsonl" | tr -d ' ')"

if [ "${DEMO_COVERAGE:-0}" = "1" ]; then
  echo
  echo "== coverage (runs the test suite: slower) =="
  show "--collect-coverage (core module)" 0 --project-root core --no-html --no-color --collect-coverage
  show "--force (re-run tests)" 0 --project-root core --no-html --no-color --collect-coverage --force
  ( cd "$ROOT" && java -jar "$JAR" --project-root core --no-html --no-color --collect-coverage --min-coverage 10 > "$WORK/cov.log" 2>&1
    printf '%-42s exit=%s  %s\n' "--min-coverage 10" "$?" "$(grep -m1 -oE 'Line Coverage:.*' "$WORK/cov.log" | sed 's/^ *//')" )
fi

echo
if [ "$fail" = "0" ]; then
  echo "All flags behaved as documented."
else
  echo "Some invocations returned an unexpected exit code - see above." >&2
fi
exit "$fail"
