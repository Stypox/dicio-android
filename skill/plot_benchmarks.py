from matplotlib import pyplot as plt
import os
import json
import math
import numpy as np

# Fit the function y = A * exp(B * x) to the data
# returns (A, B)
# From: https://mathworld.wolfram.com/LeastSquaresFittingExponential.html
def fit_exp(xs, ys):
    S_x2_y = 0.0
    S_y_lny = 0.0
    S_x_y = 0.0
    S_x_y_lny = 0.0
    S_y = 0.0
    for (x,y) in zip(xs, ys):
        S_x2_y += x * x * y
        S_y_lny += y * np.log(y)
        S_x_y += x * y
        S_x_y_lny += x * y * np.log(y)
        S_y += y
    #end
    a = (S_x2_y * S_y_lny - S_x_y * S_x_y_lny) / (S_y * S_x2_y - S_x_y * S_x_y)
    b = (S_y * S_x_y_lny - S_x_y * S_y_lny) / (S_y * S_x2_y - S_x_y * S_x_y)
    return (np.exp(a), b)

def fit_poly(xs, ys):
    return (*np.polynomial.polynomial.polyfit(xs, ys, 2), 0, 0)


benchmark_dirs = sorted(os.listdir("benchmarks/"))
benchmark_dir_names = [benchmark_dir[4:14] for benchmark_dir in benchmark_dirs]
colors = dict(zip(benchmark_dir_names, plt.colormaps.get_cmap('viridis').resampled(len(benchmark_dirs)).colors))
print(benchmark_dirs)


input_plots = {}
incremental_bar_plots = {}
incremental_graph_plots = {}

def add_item_to_input_plots(benchmark_file, item_name, benchmark_dir, value):
    key = benchmark_file + " - " + item_name
    if key not in input_plots.keys():
        input_plots[key] = ([], [])
    input_plots[key][0].append(benchmark_dir)
    input_plots[key][1].append(value)

def add_item_to_incremental_bar_plots(benchmark_file, item_name, benchmark_dir, value):
    key = benchmark_file + " - " + item_name
    if key not in incremental_bar_plots.keys():
        incremental_bar_plots[key] = ([], [])
    incremental_bar_plots[key][0].append(benchmark_dir)
    incremental_bar_plots[key][1].append(value)

def add_line_to_incremental_graph_plots(benchmark_file, item_name, benchmark_dir, xs, ys):
    key = benchmark_file + " - " + item_name
    if key not in incremental_graph_plots.keys():
        incremental_graph_plots[key] = []
    incremental_graph_plots[key].append((benchmark_dir, xs, ys))


for benchmark_file in ["current_time", "weather", "timer"]:
    for benchmark_dir, benchmark_dir_name in zip(benchmark_dirs, benchmark_dir_names):
        data = json.load(open(os.path.join("benchmarks/", benchmark_dir, benchmark_file + ".json")))

        add_item_to_incremental_bar_plots(benchmark_file, "increm", benchmark_dir_name, data["incremental"][-1]["size"])

        for benchmark in data["benchmarks"]:
            add_item_to_input_plots(benchmark_file, str(len(benchmark["input"])) + "ch", benchmark_dir_name, benchmark["time"] / 1e9)

        add_line_to_incremental_graph_plots(benchmark_file, "increm", benchmark_dir_name, [v["size"] for v in data["incremental"]], [v["time"] / 1e9 for v in data["incremental"]])


#input_plots = {}

plot_count = len(input_plots) + len(incremental_bar_plots) + len(incremental_graph_plots)
plot_height = max(int(math.sqrt(plot_count) + 0.5), 1)
plot_width = (plot_count + plot_height - 1) // plot_height
print(plot_height, plot_width)
assert plot_height * plot_width >= plot_count

for i, (item_name, (benchmark_dirs, values)) in enumerate(input_plots.items()):
    plt.subplot(plot_height, plot_width, i + 1)
    plt.yscale('log',base=10)
    plt.title(item_name, fontsize=9)
    plt.bar(benchmark_dirs, values, color=[colors[benchmark_dir] * 0.7 for benchmark_dir in benchmark_dirs])

for i, (item_name, (benchmark_dirs, values)) in enumerate(incremental_bar_plots.items()):
    plt.subplot(plot_height, plot_width, i + 1 + len(input_plots))
    plt.title(item_name, fontsize=9)
    plt.bar(benchmark_dirs, values, color=[colors[benchmark_dir] for benchmark_dir in benchmark_dirs])

for i, (item_name, data) in enumerate(incremental_graph_plots.items()):
    plt.subplot(plot_height, plot_width, i + 1 + len(input_plots) + len(incremental_bar_plots))
    plt.title(item_name, fontsize=9)
    for (benchmark_dir, xs, ys) in data:
        plt.plot(xs, ys, color=colors[benchmark_dir])

    # A, B = fit_exp(xs, ys)
    # print(f"{item_name} => O({np.exp(B):.2f}ⁿ)")
    # plt.plot(list(range(xs[-1]+1)), [A * np.exp(B * v) for v in range(xs[-1]+1)], color="black", linewidth=1, linestyle='--')

    a0, a1, a2, a3, a4 = fit_poly(xs, ys)
    print(f"{item_name} => ({a4**(1/4)*1e4:.2f}n)⁴ + ({a3**(1/3)*1e4:.2f}n)³ + ({a2**(1/2)*1e4:.2f}n)² + ({a1*1e4:.2f}n)")
    plt.plot(list(range(xs[-1]+1)), [a4*v**4 + a3*v**3 + a2*v**2 + a1*v + a0 for v in range(xs[-1]+1)], color="black", linewidth=1, linestyle='--')

plt.get_current_fig_manager().window.showMaximized()
plt.show(block=False)
plt.pause(0.01)
plt.tight_layout()
plt.pause(0.01)
plt.tight_layout()
plt.show(block=True)
