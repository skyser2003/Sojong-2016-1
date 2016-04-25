import subprocess
import argparse
import plot
import os
import os.path
import glob
import shutil
import threading
import platform

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


def run(count, setting):
    print "Run : " + setting
    pp = subprocess.Popen(default_args + ['-b', str(count), setting])
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


def write_plot(sim_name):
    plot.plot_report(
        os.path.join('reports', sim_name + '_AllSpreadReport.txt'))


def main():
    os.chdir('one_simulator')
    shutil.rmtree('reports')
    compile()
    parser = argparse.ArgumentParser()
    parser.add_argument('-np', dest='plot', action="store_false")
    parser.add_argument('count', type=int)
    parser.add_argument('settings', type=str, nargs='*',
                        default=['setcover_settings.txt',
                                 'hc_settings.txt',
                                 'random_settings.txt'])
    args = parser.parse_args()

    threads = []
    for setting in args.settings:
        thread = threading.Thread(None, run,
                                  args=(args.count, setting))
        thread.start()
        threads.append(thread)

    for thread in threads:
        thread.join()

    threads = []
    for setting in args.settings:
        if args.plot:
            sim_name = get_sim_name(setting)
            write_plot(sim_name)


if __name__ == "__main__":
    main()
