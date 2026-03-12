import logging
from logging.handlers import RotatingFileHandler
from pathlib import Path
from typing import Optional


DEFAULT_LOG_FORMAT = "%(asctime)s | %(levelname)s | %(name)s | %(message)s"
DEFAULT_LOG_FILE = "pipeline.log"
_IS_CONFIGURED = False


def setup_logging(
    level: int = logging.INFO,
    log_dir: Optional[Path] = None,
    log_file: str = DEFAULT_LOG_FILE
) -> None:
    """
    Configure application-wide logging once.
    """

    global _IS_CONFIGURED

    if _IS_CONFIGURED:
        return

    resolved_log_dir = (
        Path(log_dir)
        if log_dir is not None
        else Path(__file__).resolve().parent.parent / "logs"
    )
    resolved_log_dir.mkdir(parents=True, exist_ok=True)

    file_handler = RotatingFileHandler(
        resolved_log_dir / log_file,
        maxBytes=5 * 1024 * 1024,
        backupCount=3
    )
    console_handler = logging.StreamHandler()

    logging.basicConfig(
        level=level,
        format=DEFAULT_LOG_FORMAT,
        handlers=[file_handler, console_handler]
    )

    _IS_CONFIGURED = True


def get_logger(name: str) -> logging.Logger:
    """
    Return a named logger after ensuring logging is configured.
    """

    setup_logging()
    return logging.getLogger(name)
