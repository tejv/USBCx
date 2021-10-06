# Author: tsheoran
import os
import sys
import argparse
from P4 import P4,P4Exception, Progress
from datetime import datetime, timedelta, date
import subprocess

##############################################################################################
#                                User defined
##############################################################################################
P4PORT = 'p4host.europe.root.pri:1666'
WSROOT = 'C:\\vm\\anc_ws\\'
PYTHON2PATH = 'C:\\Python27\\python.exe'
PYTHON3PATH = 'C:\\Python39\\python.exe'

# Create new mappings if path changes
def get_branchview(mapping_name, fname, uname):
    if mapping_name == 'anc_03_dev':
        basepath = '//depot/bg/Features/caa_fastpair'
        view = [f"{basepath}/03_Development/adk/... {basepath}/01_Private/{uname}/{fname}/adk/...",
                f"{basepath}/03_Development/earbud/... {basepath}/01_Private/{uname}/{fname}/earbud/...",
                f"{basepath}/03_Development/headset/... {basepath}/01_Private/{uname}/{fname}/headset/..."]
        return view
    if mapping_name == 'vm_main_dev':
        basepath = '//depot/bg'
        view = [f"{basepath}/vm_main/adk/... {basepath}/private/{uname}/{fname}/adk/...",
                f"{basepath}/vm_main/earbud/... {basepath}/private/{uname}/{fname}/earbud/...",
                f"{basepath}/vm_main/headset/... {basepath}/private/{uname}/{fname}/headset/..."]
        return view
    raise Exception("Mapping not found")

###############################################################################################

def execute_cmd(cmd):
    process = subprocess.Popen(cmd.split(), shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                               universal_newlines=True)
    while process.poll() is None:
        readline = process.stdout.readline().rstrip()
        if readline:
            print(readline)
    for line in process.stdout.readlines():
        print(line.rstrip())
    for line in process.stderr.readlines():
        print(line.rstrip())
    return process.returncode

def get_ws_name(uname, fname):
    return f"{uname}_{fname}_ws"

def create_feature_branch(param_dict):
    fname, uname, mapping = param_dict['foldername'], param_dict['username'], param_dict['mappingname']
    try:
        p4 = P4()
        p4.user = param_dict['username']
        p4.port = P4PORT
        p4.connect()
        print(p4)
        p4.client = get_ws_name(uname, fname)
        branch = p4.fetch_branch(fname)
        bview = get_branchview(mapping, fname, uname)
        branch["View"] = bview
        p4.save_branch(branch)
        print("Integrating...")
        print(f'Branch Mapping: {bview}')
        p4.run('integrate', '-b', fname)
        jobName = f"{uname}_{fname}"
        print(f"Creating job {jobName} ...")
        job = p4.fetch_job(jobName)
        job['Description'] = jobName
        p4.save_job(job)
        print(job)
        print('Submitting changes...')
        try:
            p4.run('submit', '-d', f"Integration from {bview[0]}")
        except P4Exception:
            for e in p4.errors:
                if 'p4 fix -c' in e:
                    changelist = e.split('p4 fix -c')[1].strip().split()[0]
                    print(f'Changelist no: {changelist}')
                    p4.run('fix', '-c', changelist, jobName)
                    p4.run('submit', '-c', changelist)
                    break
        p4.disconnect()
    except P4Exception:
        for e in p4.errors:
            print(e)
        p4.disconnect()
        raise Exception("Abort")

# Create or update if workspace already present
def create_local_ws(param_dict):
    fname, uname, mapping = param_dict['foldername'], param_dict['username'], param_dict['mappingname']
    try:
        p4 = P4()  # Create the P4 instance
        p4.user = param_dict['username']
        p4.port = P4PORT
        p4.connect()
        print(p4)
        spec = p4.fetch_client()
        wsname = get_ws_name(uname, fname)
        print("Creating new workspace:", wsname)
        spec["Root"] = WSROOT + wsname
        spec["Owner"] = uname
        map = get_branchview(mapping, fname, uname)[0].split('...')[1].strip().split(f'/{uname}/')[0]
        map = [f"{map}/{uname}/{fname}/... //{wsname}/..."]
        print(f'Mapping: {map}')
        spec["View"] = map
        spec["Options"] = 'noallwrite noclobber compress unlocked nomodtime normdir'
        spec["Client"] = wsname
        p4.save_client(spec)
        p4.client = wsname
        p4.disconnect()
    except P4Exception:
        for e in p4.errors:
            print(e)
        p4.disconnect()
        raise Exception("Abort")

def run_platform_creator(param_dict):
    print("Running Platform Creator...")
    fname, uname, chip_family = param_dict['foldername'], param_dict['username'], param_dict['chipfamily']
    wsname = get_ws_name(uname, fname)
    runpath = WSROOT + f'{wsname}\\earbud\\workspace'
    cmd = f'{PYTHON3PATH} ..\\..\\adk\\tools\\private\\platform_creator\\platform_creator.py -c {chip_family}'
    print(f'Path: {runpath}')
    print(f'Cmd: {cmd}')
    os.chdir(runpath)
    # ret = execute_cmd(cmd)
    # print(ret)
    os.system(cmd)

def run_image_extractor(param_dict):
    print("Running Image extractor...")
    fname, uname, chip_family = param_dict['foldername'], param_dict['username'], param_dict['chipfamily']
    wsname = get_ws_name(uname, fname)
    runpath = WSROOT + f'{wsname}\\earbud\\workspace'
    cmd = f'{PYTHON3PATH} ..\\..\\adk\\tools\\private\\image_extractor\\image_extractor.py -c {chip_family}'
    print(f'Path: {runpath}')
    print(f'Cmd: {cmd}')
    os.chdir(runpath)
    os.system(cmd)


# python create_anc_ws.py -n name -c chip_family -u username
if __name__== "__main__":
    start_time = datetime.now()
    print("Script Start time:" + start_time.strftime("%Y-%m-%d %H:%M:%S"))

    parser = argparse.ArgumentParser(description='Create perforce workspace for anc project. E.g python create_anc_ws.py -n name -c chip_family -u username')
    parser.add_argument('-n', '--foldername', default='anctest-ts6',help='jira name')
    parser.add_argument('-c', '--chipfamily', default='qcc517x_qcc307x', help='Chip family, default is qcc517x_qcc307x')
    parser.add_argument('-u', '--username', default='ts40', help='User name, default is ts40')
    parser.add_argument('-m', '--mappingname', default='vm_main_dev', help='Mapping name, default is anc_04_development')
    args, unknown = parser.parse_known_args()

    param_dict = {}
    param_dict['foldername'], param_dict['chipfamily'], param_dict['username'] = args.foldername, args.chipfamily, args.username
    param_dict['mappingname'] = args.mappingname
    print(param_dict)

    #create_local_ws(param_dict)
    #create_feature_branch(param_dict)

    os.environ["P4CLIENT"] = get_ws_name(param_dict['username'],param_dict['foldername'])
    run_platform_creator(param_dict)
    run_image_extractor(param_dict)
    
    print("Hurray!!! All steps done")
    timediff = datetime.now() - start_time
    print("Script over in: " + str(timediff))




