"""
Ollama Grader - LLM-based code grading via Ollama CLI.
"""

import json
import os
import re
import subprocess
import tempfile
import time
import logging

logger = logging.getLogger(__name__)


def call_ollama(prompt: str, model: str, timeout: int) -> tuple:
    """Call Ollama CLI, return (response, error)."""
    with tempfile.NamedTemporaryFile("w", delete=False, encoding='utf-8', suffix=".txt") as tmp:
        tmp.write(prompt)
        tmpname = tmp.name

    try:
        with open(tmpname, "r", encoding="utf-8") as f:
            proc = subprocess.run(
                ["ollama", "run", model],
                stdin=f, capture_output=True, text=True, timeout=timeout
            )

        if proc.returncode != 0:
            return None, f"Exit code {proc.returncode}: {proc.stderr}"
        return proc.stdout.strip(), None

    except subprocess.TimeoutExpired:
        return None, f"Timeout after {timeout}s"
    except FileNotFoundError:
        return None, "Ollama not found in PATH"
    except Exception as e:
        return None, str(e)
    finally:
        try:
            os.remove(tmpname)
        except:
            pass


def extract_json(text: str) -> dict | None:
    """Extract first JSON object from text."""
    match = re.search(r'\{.*\}', text, flags=re.DOTALL)
    if not match:
        return None
    try:
        return json.loads(match.group(0))
    except json.JSONDecodeError:
        return None


def run_grading(samples: list, template_path: str, model: str,
                timeout: int, retries: int, output_path: str) -> list:
    """Grade all samples with Ollama, save results to JSON."""

    template = open(template_path, encoding='utf-8').read()
    logger.info(f"Grading {len(samples)} submissions with {model}")

    results = []
    for i, sample in enumerate(samples, 1):
        sid = sample["submission_id"]
        prompt = template.replace("{CODE}", sample["code"])

        logger.info(f"[{i}/{len(samples)}] Grading {sid}...")

        # Try with retries
        result = None
        for attempt in range(retries + 1):
            if attempt > 0:
                time.sleep(1)

            response, error = call_ollama(prompt, model, timeout)
            if error:
                logger.debug(f"Attempt {attempt} failed: {error}")
                continue

            parsed = extract_json(response)
            if parsed:
                result = parsed
                result["_submission_id"] = sid
                result["_model"] = model
                break

        if result is None:
            result = {"_submission_id": sid, "_error": error or "JSON parse failed"}
            logger.warning(f"Failed: {sid}")

        results.append(result)

    # Save
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(results, f, indent=2)

    success = sum(1 for r in results if "_error" not in r)
    logger.info(f"Grading done: {success}/{len(results)} successful -> {output_path}")
    return results
