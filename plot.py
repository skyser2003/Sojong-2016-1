import json
import os
import sys

import matplotlib.pyplot as pyplot


def plot_report(input_filename):
    with open(input_filename) as f:
        points = list()
        for line in f:
            point = json.loads(line)
            points.append(point)
        times = [point['time'] for point in points]
        current_spread = [point['current_spread'] for point in points]
        pyplot.plot(times, current_spread)
        result_filename = os.path.splitext(input_filename)[0] + '.png'
        pyplot.savefig(result_filename)


def main():
    if len(sys.argv) != 2:
        print 'argument needed'
        sys.exit(128)

    plot_report(sys.argv[1])

if __name__ == "__main__":
    main()
