import json, re

def jsonFileToDict(file):
    jsonData = open(file)
    data = json.load(jsonData)
    jsonData.close()

    return data

def dictToJsonFile(data, file):
    f = open(file, 'w')
    f.write(json.dumps(data))

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
        d['pantry'] = d['pantry'].encode('ascii', 'replace').lower()
        d['refrigerator'] = d['refrigerator'].encode('ascii', 'replace').lower()
        d['freezer'] = d['freezer'].encode('ascii', 'replace').lower()

data = {}
if not True:
    # This will not execute, see cleanUpJson()
    data = jsonFileToDict('raw.json')['data']
    cleanUpJson(data)
    dictToJsonFile({ 'data': data }, 'clean.json')
else:
    data = jsonFileToDict('clean.json')['data']

# Sort the data based on the name
data = sorted(data, key=lambda x: x['name'])

# Here I'm comparing the length of the name before the comma and the name before
# the -. I take the shorter one, which should be the proper name of the item
def getFoodName(s):
    if len(s.split(',',2)[0].strip(',- ')) < len(s.split('-',2)[0].strip(',- ')):
        return s.split(',',2)
    else:
        return s.split('-',2)

# Return the average of a date span. Ex. 3 -> 3, 3-5 -> 4, 10-20 -> 15
def getStrListAvg(exp):
    if len(exp) == 2:
        return (int(exp[0]) + int(exp[1])) / 2
    else:
        return int(exp[0])

def convertExpiryAmountToDays(expiry):
    if expiry == '': # For those without dates
        return -1   
    elif expiry.find('day') != -1: # Ex. "1 day", "7 days", "3-5 days"
        exp = expiry[:expiry.find(' ')].split('-')
        return getStrListAvg(exp)
    elif expiry.find('week') != -1: # Ex. "1 week", "3 weeks", "6-8 weeks"
        exp = expiry[:expiry.find(' ')].split('-')
        return getStrListAvg(exp) * 7
    elif expiry.find('month') != -1: # Ex. "1 month", "2 months", "7-10 months"
        exp = expiry[:expiry.find(' ')].split('-')
        return getStrListAvg(exp) * 30
    elif expiry.find('year') != -1: # You get the point by now
        exp = expiry[:expiry.find(' ')].split('-')
        return getStrListAvg(exp) * 365
    elif expiry.find('keeps indefinitely') != -1: # Found several with this date set (can be an easter egg)
        return 12345
    elif expiry.find('hour') != -1: # I found one that expires in an hour
        return 0
    elif expiry.find('date on package') != -1: # Another case found
        return -1
    else: # nothing should go this far
        return -1

# Parse the data here
parsedData = []
posOfName = {}
for d in data:
    item = {}
    item['full_name'] = d['name']
    name = getFoodName(d['name']) # name here is actually a list, with the first part being the name, and the second part being the description
    item['name'] = name[0].strip().replace('/', '').replace('(', '').replace(')', '')
    item['name'] = re.sub(' +', ' ', item['name']) # Removes duplicate spaces
    print item['name']

    #Here we check if the description contains any of the unwanted tags. May need to add more tags
    if len(name) > 1:
        if name[1].find('unopened') != -1 and name[1].find('unopened or opened') == -1:
            continue
        elif name[1].find('cut') != -1:
            continue

    # seeing if the item is already in the database
    # the following needs code to parse the adjectives and categorize it accordingly. This means
    # adding more similar blocks for different adjectives in the database
    n = 0
    if not item['name'] in posOfName:
        parsedData.append(item)
        n = len(parsedData) - 1
        posOfName[item['name']] = n
    else:
        n = posOfName[item['name']]

    item = parsedData[n] # This isn't a new object, just a reference to the one in our data

    # Initialize dictionary if not set already
    if not 'expiry' in item:
        item['expiry'] = []

    pantry = convertExpiryAmountToDays(d['pantry'])
    refrigerator = convertExpiryAmountToDays(d['refrigerator'])
    freezer = convertExpiryAmountToDays(d['freezer'])

    expiry = {}
    if len(name) > 1:
        if name[1].find('commercially frozen') != -1:
            expiry['type'] = 'frozen'
        elif name[1].find('commercially canned') != -1:
            expiry['type'] = 'canned'
        elif name[1].find('fresh, raw') != -1:
            expiry['type'] = 'fresh'
        elif name[1].find('sold in refrigerated') != -1:
            expiry['type'] = 'refContainer'
        elif name[1].find('from concentrate') != -1:
            if name[1].find('not from concentrate') != -1:
                expiry['type'] = 'notConcentrate'
            else:
                expiry['type'] = 'concentrate'
        elif name[1].find('dried') != -1:
            expiry['type'] = 'dried'
        else:
            expiry['type'] = 'default'
    else:
        expiry['type'] = 'default'

    expiry['pantry'] = pantry
    expiry['refrigerator'] = refrigerator
    expiry['freezer'] = freezer
    item['expiry'].append(expiry)

# Input must be sorted
def getAlphabetPositions(parsedData):
    positions = []
    letter = 'a'

    positions.append({ 'letter': letter, 'pos': 0 })

    for x, item in enumerate(parsedData):
        if item['name'][0] != letter:
            letter = item['name'][0]
            positions.append({ 'letter': letter, 'pos': x })

    return positions

# Turns out that we don't really need the alphabet positons, the SQL DB handles that
dictToJsonFile({ 'data': parsedData, 'alphabetPos' : getAlphabetPositions(parsedData) }, 'parsed.json')