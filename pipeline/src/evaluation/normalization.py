"""
Grade Normalization - Convert letter grades to numeric scores.
"""

import json
import os
import logging

logger = logging.getLogger(__name__)


def normalize_human_grades(samples: list, grade_map: dict, output_path: str) -> dict:
    """
    Convert human letter grades to numeric, aggregate by dimension.

    Returns: {submission_id: {dimension: avg_score}}
    """
    result = {}

    for sample in samples:
        sid = sample["submission_id"]
        result[sid] = {}

        # Collect scores per skill
        skill_scores = {}
        for row in sample.get("human_rows", []):
            skill = row.get("skill")
            grade = (row.get("grade") or "").strip()

            if skill and grade and grade in grade_map:
                skill_scores.setdefault(skill, []).append(grade_map[grade])

        # Average per skill
        for skill, scores in skill_scores.items():
            result[sid][skill] = sum(scores) / len(scores)

    # Save
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(result, f, indent=2)

    logger.info(f"Normalized grades for {len(result)} submissions -> {output_path}")
    return result
