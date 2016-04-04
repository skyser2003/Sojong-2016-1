import subprocess
import argparse
import plot
import os
import os.path
import glob
import shutil
import platform

sep = ';' if platform.system() == 'Windows' else ':'

default_args = ['java', '-Xmx512M',
                '-cp', sep.join([
                    'target',
                    'lib/ECLA.jar',
                    'lib/DTNConsoleConnection.jar',
                    'lib/json-simple-1.1.1.jar']),
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


def compile():
    print 'Compile'
    if not os.path.isdir('target'):
        os.mkdir('target')
    sources = [item
               for d in ['core', 'movement', 'report', 'routing', 'gui/*.java',
                         'input', 'applications', 'interfaces']
               for item in glob.glob(os.path.join('src', d, '*.java'))]
    subprocess.check_call(['javac', '-sourcepath', 'src', '-d', 'target',
                           '-extdirs', 'lib/'] + sources)
    if not os.path.isdir('target/gui/buttonGraphics'):
        shutil.copytree('src/gui/buttonGraphics', 'target/gui/buttonGraphics')


def main():
    os.chdir('one_simulator')
    compile()
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
