"""
Data Preparation - Load submissions and human grades.
"""

import csv
import json
import os
import logging

logger = logging.getLogger(__name__)


def prepare_data(samples_csv: str, submissions_dir: str, output_path: str) -> list:
    """
    Load student submissions and human grades, save as JSON.

    Returns list of sample dicts with: submission_id, code, human_rows
    """
    # Get participant IDs from CSV
    participant_ids = set()
    with open(samples_csv, newline='', encoding='utf-8') as f:
        for row in csv.DictReader(f):
            pid = row.get("participant_id", "").strip()
            if pid:
                participant_ids.add(pid)

    participant_ids = sorted(participant_ids, key=lambda x: int(x) if x.isdigit() else 0)
    logger.info(f"Found {len(participant_ids)} participants")

    samples = []
    for pid in participant_ids:
        # Find submission folder
        folder = None
        for name in os.listdir(submissions_dir):
            if f"Submission_{pid}" in name:
                folder = os.path.join(submissions_dir, name)
                break

        if not folder:
            logger.warning(f"No folder for participant {pid}")
            continue

        # Read all Java files
        code_parts = []
        for root, _, files in os.walk(folder):
            for fname in sorted(files):
                if fname.endswith(".java"):
                    path = os.path.join(root, fname)
                    text = open(path, encoding='utf-8', errors='replace').read()
                    code_parts.append(f"// --- {fname} ---\n{text}\n")

        # Get human grades from CSV
        human_rows = []
        with open(samples_csv, newline='', encoding='utf-8') as f:
            for row in csv.DictReader(f):
                if row.get("participant_id", "").strip() == pid:
                    human_rows.append(dict(row))

        samples.append({
            "submission_id": pid,
            "code": "\n".join(code_parts),
            "human_rows": human_rows
        })

    # Save
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(samples, f, indent=2)

    logger.info(f"Prepared {len(samples)} samples -> {output_path}")
    return samples
