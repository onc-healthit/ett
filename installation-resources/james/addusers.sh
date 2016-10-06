# usage: addusers.sh domainname
# adds the domain and then the set of users
# please test this as james seem to mess up the passwords

echo adding domain $1
sudo ./james-cli.sh -h localhost -p 9999 adddomain $1

read a

echo adding users for $1
sudo ./james-cli.sh -h localhost -p 9999 adduser unpublishedwellformed1@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser wellformed14@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser imaptesting@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser poptesting@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser vendoraccount@$1 vendortesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser vendor1smtpsmtp@$1 vendortesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser b1-ambulatory@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser b1-inpatient@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser b2-ambulatory@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser b2-inpatient@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser b5-ambulatory@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser b5-inpatient@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser b9-ambulatory@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser b9-inpatient@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser multipleattachments@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser xdmbadxhtml@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser xdmmimetypes@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser badccda@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser negativetestingccds@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser negativetestingcareplan@$1 smtptesting123
sudo ./james-cli.sh -h localhost -p 9999 adduser failure15@$1 smtptesting123

sudo ./james-cli.sh -h localhost -p 9999 adduser admin@$1 admintesting123

echo Added users to domain $1. Listing users.. press any key to continue and verify
echo please test the passwords as sometimes the accounts are added but passwords are not right
read a

sudo ./james-cli.sh -h localhost -p 9999 listdomains
sudo ./james-cli.sh -h localhost -p 9999 listusers
