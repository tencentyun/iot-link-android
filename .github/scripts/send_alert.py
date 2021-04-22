import base64
import datetime
import json
import logging
import os
import shlex
import ssl
import subprocess
import sys
import urllib.request
from urllib.error import URLError

logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s %(name)s:%(levelname)s:%(message)s')


def send(labels, url=None, annotations=None):
    print("========= begin send alert =========")
    alert = {
        'generatorURL':
            url,  # 格式不对会报错, 可为 null or ''
        'labels':
            labels,
        'annotations':
            annotations,
        'startsAt':
            datetime.datetime.now().astimezone(datetime.timezone(datetime.timedelta(hours=8))).isoformat()
    }
    print('===============time : ' + datetime.datetime.now().astimezone(datetime.timezone(datetime.timedelta(hours=8))).isoformat())
    # auth = base64.b64encode(f"user:password".encode()).decode("ascii")
    headers = {
        'Content-type': 'application/json',
        'Authorization': f'Basic {os.environ["ALERTMANANGER_BASIC_AUTH"]}'
    }
    data = [alert]
    req = urllib.request.Request('https://alert.goodow.com/api/v2/alerts',
                                 data=json.dumps(data).encode('utf-8'),
                                 headers=headers)
    try:
        resp = urllib.request.urlopen(req)
        print('========= urlopen 1')
    except URLError as e:
        logging.info(f'retry, {type(e.reason)}({e.reason})')
        # [Errno 104] Connection reset by peer
        # [Errno 110] Connection timed out
        # if isinstance(e.reason, ConnectionResetError):
        headers['Host'] = 'alert.goodow.com'
        req = urllib.request.Request('https://119.29.125.143:443/api/v2/alerts',
                                     data=json.dumps(data).encode('utf-8'),
                                     headers=headers)
        ctx = ssl.create_default_context()
        ctx.check_hostname = False
        ctx.verify_mode = ssl.CERT_NONE
        resp = urllib.request.urlopen(req, context=ctx)
        print('========= urlopen 2')

    if resp.status != 200:
        print('发送 Alert 出错', resp.status, resp.reason)
        raise Exception(f'{resp.status} {resp.reason}')
    print("========= end send alert =========")



def short_sha():
    process = subprocess.run(
        shlex.split(f'git rev-parse --short {os.environ["GITHUB_SHA"]}'),
        capture_output=True,
        text=True)
    return process.stdout.strip()


if __name__ == '__main__':
    github = json.loads(os.environ['GITHUB_CONTEXT'])
    job = json.loads(os.environ['JOB_CONTEXT'])
    labels = {
        'alertname': 'github workflow',
        'status': job['status'],  # job.status 报错: 'dict' object has no attribute 'status'
        'repo': github['repository'],
        'workflow': github['workflow'],
        'git_sha': short_sha(),
    }
    head_commit = github['event']['head_commit']
    url = f'{head_commit["url"]}/checks'
    annotations = {'commit': head_commit['message'], 'receivers': '25088358528,ArchurSpace'}
    print('========= start ===========')
    send(labels, url=url, annotations=annotations)
    print('========= stop ============')
