from lxml import etree

root = etree.parse("/Users/ksrinivs/Downloads/drugbank.xml")
rows = root.xpath('/x:drugbank/x:drug[./x:transporters/x:transporter/x:polypeptide/x:external-identifiers/x:external-identifier/x:resource/text()="UniProtKB"]', namespaces={'x': 'http://www.drugbank.ca'})

result = '<?xml version="1.0"?>'
for row in rows:
    drug = row.xpath('./x:name/text()', namespaces={'x': 'http://www.drugbank.ca'})
    transporters = row.xpath('./x:transporters/x:transporter', namespaces={'x': 'http://www.drugbank.ca'})
    for transporter in transporters:
        actions = transporter.xpath('./x:actions/x:action', namespaces={'x': 'http://www.drugbank.ca'})
        for action in actions:
            act = action.xpath('./text()', namespaces={'x': 'http://www.drugbank.ca'})
            id = transporter.xpath('./x:polypeptide/x:external-identifiers/x:external-identifier[./x:resource/text()="UniProtKB"]/x:identifier/text()', namespaces={'x': 'http://www.drugbank.ca'})
            result += "<row>" + "<drug>" + drug[0] + "</drug> <id>" + id[0] + "</id> <action>" + act[0] + "</action> </row>"

