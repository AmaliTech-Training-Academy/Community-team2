from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
OUTPUT_DIR = PROJECT_ROOT / "sample-data"
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
