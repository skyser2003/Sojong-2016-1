import json
import os
import sys

import matplotlib.pyplot as pyplot


def read_report(filename):
    with open(filename) as f:
        points = list()
        for line in f:
            point = json.loads(line)
            points.append(point)

        return points


def plot_report(points, output_filename):
    times = [point['time'] for point in points]
    current_spread = [point['current_spread'] for point in points]
    p = pyplot.plot(times, current_spread)
    if output_filename:
        pyplot.savefig(output_filename)
        pyplot.cla()
    return p


def plot_stastics(data, output_filename):
    i = 0
    plts = []
    names = []
    for k, v in data.items():
        mean = v['mean']
        err_min = mean - v['min']
        err_max = v['max'] - mean
        p = pyplot.errorbar([i / len(data)], [mean],
                            yerr=[[err_min], [err_max]])
        plts.append(p)
        names.append(k)
        i = i + 1
    pyplot.legend(plts, names)
    pyplot.savefig(output_filename)


def main():
    if len(sys.argv) != 2:
        print 'argument needed'
        sys.exit(128)

    plot_report(sys.argv[1])

if __name__ == "__main__":
    main()
