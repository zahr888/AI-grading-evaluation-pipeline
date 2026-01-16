"""
Visualization - Generate evaluation plots.
"""

import os
import logging
import matplotlib.pyplot as plt

logger = logging.getLogger(__name__)


def generate_plots(metrics: dict, output_dir: str, run_id: str) -> list:
    """Generate MAE and accuracy bar charts."""

    os.makedirs(output_dir, exist_ok=True)
    per_dim = metrics.get("per_dimension", {})
    dims = list(per_dim.keys())

    plots = []

    # MAE Plot
    maes = [per_dim[d].get("MAE") or 0 for d in dims]
    fig, ax = plt.subplots(figsize=(10, 6))
    ax.bar(dims, maes, color='steelblue')
    ax.set_title('MAE by Dimension')
    ax.set_ylabel('MAE (points)')
    plt.xticks(rotation=30, ha='right')
    plt.tight_layout()

    mae_path = os.path.join(output_dir, f"{run_id}_mae.png")
    plt.savefig(mae_path, dpi=150)
    plt.close()
    plots.append(mae_path)

    # Accuracy Plot
    accs = [per_dim[d].get("accuracy_pct") or 0 for d in dims]
    fig, ax = plt.subplots(figsize=(10, 6))
    ax.bar(dims, accs, color='forestgreen')
    ax.set_title(f'Accuracy within {metrics["overall"]["threshold_used"]} Points')
    ax.set_ylabel('Accuracy (%)')
    ax.set_ylim(0, 105)
    plt.xticks(rotation=30, ha='right')
    plt.tight_layout()

    acc_path = os.path.join(output_dir, f"{run_id}_accuracy.png")
    plt.savefig(acc_path, dpi=150)
    plt.close()
    plots.append(acc_path)

    logger.info(f"Generated {len(plots)} plots -> {output_dir}")
    return plots
