import subprocess
import argparse
import plot
from StringIO import StringIO
import os
import os.path

default_args = ['java', '-Xmx512M',
                '-cp', 'target:lib/ECLA.jar'
                ':lib/DTNConsoleConnection.jar'
                ':lib/json-simple-1.1.1.jar',
                'core.DTNSim']


def run(count, setting, write_plot):
    print "Run : " + setting
    pp = subprocess.Popen(default_args + ['-b', str(count), setting],
                          stdout=subprocess.PIPE)
    sim_name = None
    for line in pp.stdout:
        print line.strip()
        if not sim_name and line.startswith('Running simulation '):
            sim_name = line.strip().split("'", 1)[1][:-1]
    if write_plot:
        print "write plot"
        plot.plot_report(
            os.path.join('reports', sim_name + '_AllSpreadReport.txt'))


def main():
    os.chdir('one_simulator')
    parser = argparse.ArgumentParser()
    parser.add_argument('-p', dest='plot', action="store_true")
    parser.add_argument('count', type=int)
    parser.add_argument('settings', type=str, nargs='*',
                        default=['setcover_settings.txt',
                                 'hc_settings.txt',
                                 'random_settings.txt'])
    args = parser.parse_args()

    for setting in args.settings:
        run(args.count, setting, args.plot)


if __name__ == "__main__":
    main()
