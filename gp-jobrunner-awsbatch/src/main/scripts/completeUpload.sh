#!/bin/bash

echo "Complete upload: $@ "

# add an AWS profile statement if needed
if [ "$2" ]; then
   PROFILE=" --profile $2 "
   echo "Profile is $PROFILE"
else  
   PROFILE=""
fi

/Users/liefeld/AnacondaProjects/CondaInstall/anaconda3/bin/aws s3api complete-multipart-upload --cli-input-json ${1}   --profile genepattern

echo /Users/liefeld/AnacondaProjects/CondaInstall/anaconda3/bin/aws s3api complete-multipart-upload --cli-input-json ${1}   --profile genepattern


# remove the temp input file to avoid wasting space
rm $IN_FILE
