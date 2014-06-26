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
parsedData = {}
for d in data:
    s = d['name']
    name = None
    # Here I'm comparing the length of the name before the comma and the name before
    # the -. I take the shorter one, which should be the proper name of the item
    if len(s.split(',',2)[0].strip(',- ')) < len(s.split('-',2)[0].strip(',- '))
        name = s.split(',',2)
    else
        name = s.split('-',2)
    # name here is actually a list, with the first part being the name, and the second part being the description

    #Here we check if the description contains any of the unwanted tags. May need to add more tags
    if name[1].find('UNOPENED') != -1 && name[1].find('UNOPENED OR OPENED') == -1
        continue
    elif name[1].find('CUT') != -1
        continue

    # cleaning up the name
    name[0] = name[0].strip(',- ')
    # seeing if the item is already in the database
    # the following needs code to parse the adjectives and categorize it accordingly. This means
    # adding more similar blocks for different adjectives in the database
    if not name[0] in parsedData
        parsedData[name[0]] = {}
    if name[1].find('COMMERCIALLY FROZEN') != -1
        parsedData[name[0]]['frozen'] = {}
        parsedData[name[0]]['frozen']['pantry'] = d['pantry']
        parsedData[name[0]]['frozen']['refrigerator'] = d['refrigerator']
        parsedData[name[0]]['frozen']['freezer'] = d['freezer']
    elif name[1].find('COMMERCIALLY CANNED') != -1
        parsedData[name[0]]['canned'] = {}
        parsedData[name[0]]['canned']['pantry'] = d['pantry']
        parsedData[name[0]]['canned']['refrigerator'] = d['refrigerator']
        parsedData[name[0]]['canned']['freezer'] = d['freezer']
    elif name[1].find('FRESH, RAW') != -1
        parsedData[name[0]]['fresh'] = {}
        parsedData[name[0]]['fresh']['pantry'] = d['pantry']
        parsedData[name[0]]['fresh']['refrigerator'] = d['refrigerator']
        parsedData[name[0]]['fresh']['freezer'] = d['freezer']
    elif name[1].find('SOLD IN REFRIGERATED') != -1
        parsedData[name[0]]['refContainer'] = {}
        parsedData[name[0]]['refContainer']['pantry'] = d['pantry']
        parsedData[name[0]]['refContainer']['refrigerator'] = d['refrigerator']
        parsedData[name[0]]['refContainer']['freezer'] = d['freezer']
    elif name[1].find('FROM CONCENTRATE') != -1
        if name[1].find('NOT FROM CONCENTRATE') != -1
            parsedData[name[0]]['notConcentrate'] = {}
            parsedData[name[0]]['notConcentrate']['pantry'] = d['pantry']
            parsedData[name[0]]['notConcentrate']['refrigerator'] = d['refrigerator']
            parsedData[name[0]]['notConcentrate']['freezer'] = d['freezer']
        else
            parsedData[name[0]]['concentrate'] = {}
            parsedData[name[0]]['concentrate']['pantry'] = d['pantry']
            parsedData[name[0]]['concentrate']['refrigerator'] = d['refrigerator']
            parsedData[name[0]]['concentrate']['freezer'] = d['freezer']
    elif name[1].find('DRIED') != -1
        parsedData[name[0]]['DRIED'] = {}
        parsedData[name[0]]['DRIED']['pantry'] = d['pantry']
        parsedData[name[0]]['DRIED']['refrigerator'] = d['refrigerator']
        parsedData[name[0]]['DRIED']['freezer'] = d['freezer']
    else
        parsedData[name[0]]['default'] = {}
        parsedData[name[0]]['default']['pantry'] = d['pantry']
        parsedData[name[0]]['default']['refrigerator'] = d['refrigerator']
        parsedData[name[0]]['default']['freezer'] = d['freezer']



