#!/usr/bin/env python
'''
    This script is used for get/update patch delivery tools and Do patch delivery
    Example:
        python patch_delivery_cli.py
'''
import os
import sys
import commands
import shutil
import re
import optparse
import signal
import subprocess
import threading
import logging
import getopt


global tools_path
global git_path


def init():
    global tools_path,git_path
    home = os.getenv("HOME")
    tools_path = home + "/.PatchDeliveryTool"
    git_path = home + "/.PatchDeliveryTool/pdc_tools"
    if not os.path.exists(tools_path):
        #print("mkdir %s " % tools_path)
        os.mkdir(tools_path)

def check_env():
    pathV = os.getenv("PATH")


def patch_delivery():
    global git_path
    need_clone = 1
    #print(tools_path)
    if not os.path.exists(tools_path):
        print("mkdir %s " % tools_path)
        os.mkdir(tools_path)
        need_clone = 1
    elif not os.path.exists(git_path):
        need_clone = 1
    elif not os.path.exists(git_path + "/.git"):
        shutil.rmtree(git_path)
        need_clone = 1
    else:
        need_clone = 0
    current_path = os.getcwd()
    if need_clone == 1:
        print("Downloading patch delivery tools ...")
        os.chdir(tools_path)
        result = execmd (['git','clone','http://172.16.11.162:8081/tools/pdc_tools','-b','master'],300)[2]
        if result != 0:
            print("Download patch delivery tools failed!")
            print("Please contacts with Integration team.")
            exit()
    else:
        print("Updating patch delivery tools ...")
        os.chdir(git_path)
        execmd(['git','reset','--hard'])
        execmd(['git','clean','-df'])
        result = execmd(['git','pull'],30)[2]
        if result != 0:
            print("Update patch delivery tools failed")
            print("Please contacts with Integration team.")

    os.chdir(current_path)
    os.system("python " + os.path.join(git_path, "patch-delivery-gui_NB"))
def execmd(cmd, timeout=300):
    """ Execute a command in a new child process.

    The child process is killed if it timesout.

    """
    kill_check = threading.Event()

    def kill_process_after_timeout(pid):
        """Callback method to kill process `pid` on timeout."""
        p = subprocess.Popen(['ps', '--ppid', str(pid)],
                             stdout=subprocess.PIPE)
        child_pids = []
        for line in p.stdout:
            if len(line.split()) > 0:
                local_pid = (line.split())[0]
                if local_pid.isdigit():
                    child_pids.append(int(local_pid))
        os.kill(pid, signal.SIGKILL)
        for child_pid in child_pids:
            os.kill(child_pid, signal.SIGKILL)
        kill_check.set()  # tell the main routine that we had to kill
        return

    try:
        process = subprocess.Popen(cmd, stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE)
        watchdog = threading.Timer(timeout,
                                   kill_process_after_timeout,
                                   args=(process.pid, ))
        watchdog.start()
        out, err = process.communicate()
        result_out = ''.join(out).strip()
        result_err = ''.join(err).strip()
        watchdog.cancel()  # if it's still waiting to run
        if kill_check.isSet():
            result_err = "Timeout: the command \"%s\" did not " \
                         "complete in %d sec." % (" ".join(cmd), timeout)
        kill_check.clear()
        if process.poll() != 0:
            logging.error('Error executing: %s', " ".join(cmd))
        return result_out, result_err, process.poll()
    except OSError as exp:
        logging.error(exp)
    except KeyboardInterrupt:
        kill_process_after_timeout(process.pid)
        watchdog.cancel()
        return

def main():
    init()
    patch_delivery()


if __name__ == '__main__':
    main()







