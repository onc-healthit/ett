git clone https://github.com/siteadmin/2015-certification-ccda-testdata.git

cd /sites/ccda/data/b1-ambulatory
rm *.xml
cp /sites/ccda/2015-certification-ccda-testdata/Receiver\ SUT\ Test\ Data/170.315_b1_ToC_Amb/*.xml /sites/ccda/data/b1-ambulatory

cd /sites/ccda/data/b1-inpatient
rm *.xml
cp /sites/ccda/2015-certification-ccda-testdata/Receiver\ SUT\ Test\ Data/170.315_b1_ToC_Inp/*.xml /sites/ccda/data/b1-inpatient

cd /sites/ccda/data/b2-ambulatory
rm *.xml
cp /sites/ccda/2015-certification-ccda-testdata/Receiver\ SUT\ Test\ Data/170.315_b2_CIRI_Amb/*.xml /sites/ccda/data/b2-ambulatory

cd /sites/ccda/data/b2-inpatient
rm *.xml
cp /sites/ccda/2015-certification-ccda-testdata/Receiver\ SUT\ Test\ Data/170.315_b2_CIRI_Inp/*.xml /sites/ccda/data/b2-inpatient

cd /sites/ccda/data/b5-ambulatory
rm *.xml
cp /sites/ccda/2015-certification-ccda-testdata/Receiver\ SUT\ Test\ Data/170.315_b5_CCDS_Amb/*.xml /sites/ccda/data/b5-ambulatory

cd /sites/ccda/data/b5-inpatient
rm *.xml
cp /sites/ccda/2015-certification-ccda-testdata/Receiver\ SUT\ Test\ Data/170.315_b5_CCDS_Inp/*.xml /sites/ccda/data/b5-inpatient

cd /sites/ccda/data/b9-ambulatory
rm *.xml
cp /sites/ccda/2015-certification-ccda-testdata/Receiver\ SUT\ Test\ Data/170.315_b9_CP_Amb/*.xml /sites/ccda/data/b9-ambulatory

cd /sites/ccda/data/b9-inpatient
rm *.xml
cp /sites/ccda/2015-certification-ccda-testdata/Receiver\ SUT\ Test\ Data/170.315_b9_CP_Inp/*.xml /sites/ccda/data/b9-inpatient

cd /sites/ccda/data/negativetestingccds
rm *.xml
cp /sites/ccda/2015-certification-ccda-testdata/Receiver\ SUT\ Test\ Data/NegativeTesting_CCDS/*.xml /sites/ccda/data/negativetestingccds

cd /sites/ccda/data/negativetestingcareplan
rm *.xml
cp /sites/ccda/2015-certification-ccda-testdata/Receiver\ SUT\ Test\ Data/NegativeTesting_CarePlan/*.xml /sites/ccda/data/negativetestingcareplan

echo "\nDate reload completed\n"

cd /sites/ccda
rm -R 2015-certification-ccda-testdata

echo "Repository deleted\n"
