import json

def jsonFileToDict(file):
    jsonData = open(file)
    data = json.load(jsonData)
    jsonData.close()

    return data

def dictToJsonFile(data, file):
    f = open(file, 'w')
    f.write(json.dumps({ 'data': data }))

# Only run this once the first time you get crawled data from StillTasty
def cleanUpJson(data):
    for d in data:
        d['pantry'] = ''
        d['refrigerator'] = ''
        d['freezer'] = ''

        if 'condition_2' not in d:
            d['condition_2'] = ['']

        if d['condition_1'][0] == 'Pantry':
            d['pantry'] = d['condition_1_time'][0]
        elif d['condition_1'][0] == 'Refrigerator':
            d['refrigerator'] = d['condition_1_time'][0]
        elif d['condition_1'][0] == 'Freezer':
            d['freezer'] = d['condition_1_time'][0]

        if d['condition_2'][0] == 'Pantry':
            d['pantry'] = d['condition_2_time'][0]
        elif d['condition_2'][0] == 'Refrigerator':
            d['refrigerator'] = d['condition_2_time'][0]
        elif d['condition_2'][0] == 'Freezer':
            d['freezer'] = d['condition_2_time'][0]

        # Remove the old keys
        d.pop('_resultNumber', None)
        d.pop('_connectorVersionGuid', None)
        d.pop('_connectorVersionGuid', None)
        d.pop('_source', None)
        d.pop('_outputTypes', None)
        d.pop('_pageUrl', None)
        d.pop('_widgetName', None)
        d.pop('_input', None)
        d.pop('_num', None)
        d.pop('condition_1', None)
        d.pop('condition_1_time', None)
        d.pop('condition_2', None)
        d.pop('condition_2_time', None)

        # Encode to ascii
        d['name'] = d['name'][0].lower().replace(u'\u2014', '-').encode('ascii', 'replace')
        d['pantry'] = d['pantry'].encode('ascii', 'replace')
        d['refrigerator'] = d['refrigerator'].encode('ascii', 'replace')
        d['freezer'] = d['freezer'].encode('ascii', 'replace')

data = {}
if not True:
    # This will not execute, see cleanUpJson()
    data = jsonToDict('stilltasty_raw.json')['data']
    cleanUpJson(data)
    dictToJsonFile(data, 'stilltasty.json')
else:
    data = jsonFileToDict('stilltasty.json')['data']

# Parse the data here
for d in data:
    print d