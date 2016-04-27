import subprocess
import argparse
import plot
import os
import os.path
import glob
import shutil
import platform
from multiprocessing import Pool, cpu_count

cpu_count = cpu_count()

sep = ';' if platform.system() == 'Windows' else ':'

default_args = ['java', '-Xmx512M',
                '-cp', sep.join([
                    'target',
                    'lib/ECLA.jar',
                    'lib/DTNConsoleConnection.jar',
                    'lib/json-simple-1.1.1.jar']),
                'core.DTNSim']


def get_sim_name(setting):
    with open(setting, 'r') as setting_file:
        for line in setting_file:
            if line.startswith('Scenario.name'):
                return line.strip()[len('Scenario.name = '):]


def run(n, count, setting):
    print "Run : {0} ({1}/{2})".format(setting, n, count)
    pp = subprocess.Popen(default_args + ['-b', "{0}:{1}".format(n, count),
                                          'general_settings.txt', setting])
    pp.wait()


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


def write_plot(index, sim_name):
    plot.plot_report(
        os.path.join('reports',
                     '{0}_AllSpreadReport_{1}.txt'.format(sim_name, index)))


def main():
    os.chdir('one_simulator')

    if os.path.exists('reports'):
        shutil.rmtree('reports')

    os.makedirs('reports')

    compile()
    parser = argparse.ArgumentParser()
    parser.add_argument('-np', dest='plot', action="store_false")
    parser.add_argument('count', type=int)
    parser.add_argument('settings', type=str, nargs='*',
                        default=['setcover_settings.txt',
                                 'hc_settings.txt',
                                 'hc_settings1.txt',
                                 'hc_settings2.txt',
                                 'hc_settings3.txt',
                                 'hcsw_settings.txt',
                                 'random_settings.txt'])
    parser.add_argument('-t', dest='thread', type=int, default=cpu_count)
    args = parser.parse_args()

    pool = Pool(args.thread)
    for i in range(0, args.count):
        for setting in args.settings:
            pool.apply_async(run, (i + 1, args.count, setting))

    pool.close()
    pool.join()

    if args.plot:
        for setting in args.settings:
            for i in range(0, args.count):
                sim_name = get_sim_name(setting)
                write_plot(i + 1, sim_name)


if __name__ == "__main__":
    main()
