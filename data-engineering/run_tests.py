# Test runner script
#!/usr/bin/env python3

import os
import subprocess
import sys
from pathlib import Path

COMMON_PYTEST_ARGS = [
    "-p",
    "no:pytest_postgresql",
    "-p",
    "no:pytest_postgresql.plugin",
]
NO_TESTS_COLLECTED = 5


def run_pytest(*args: str) -> subprocess.CompletedProcess:
    return subprocess.run(
        [sys.executable, "-m", "pytest", *COMMON_PYTEST_ARGS, *args],
        env=os.environ.copy(),
    )


def run_tests():
    """Run the test suite with different configurations."""

    print("Running Data Engineering Tests")
    print("=" * 50)

    project_root = Path(__file__).resolve().parent
    os.chdir(project_root)

    print("Installing test dependencies...")
    subprocess.run(
        [sys.executable, "-m", "pip", "install", "-r", "requirements-test.txt"],
        check=False,
    )

    print("Running unit tests...")
    result_unit = run_pytest("-m", "unit", "-v", "--tb=short")

    print("Running integration tests...")
    result_integration = run_pytest("-m", "integration", "-v", "--tb=short")
    if result_integration.returncode == NO_TESTS_COLLECTED:
        print("No integration tests collected. Treating as success.")
        result_integration = subprocess.CompletedProcess(
            result_integration.args,
            0,
        )

    print("Generating coverage report...")
    result_coverage = run_pytest("--cov=.", "--cov-report=html", "--cov-report=term")
    if result_coverage.returncode == NO_TESTS_COLLECTED:
        print("No tests collected for coverage run.")

    print("\n" + "=" * 50)
    if result_unit.returncode == 0 and result_integration.returncode == 0:
        print("All tests passed!")
        return 0

    print("Some tests failed!")
    return 1


if __name__ == "__main__":
    sys.exit(run_tests())
