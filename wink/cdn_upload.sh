#!/bin/bash

check_result() {
	http_code=`curl -I -m 10 -o /dev/null -s -w %{http_code} -L https://download.immomo.com/android/$file_name`
	echo "http status :" $http_code
	if [ $http_code  -eq  200 ] 
	then
		echo "cdn sync succeed, download url is https://download.immomo.com/android/$file_name"
	else
		echo "cdn sync waiting"
		sleep 4
		check_result
	fi
}

file_name=$1
echo "file to upload is "$file_name
# echo `curl -X POST https://tickets.wemomo.com/api/open/ftp/api/uploadfile/ \-F "secret_key=9094c7970c9b28ce9822af586101410e" -F "username=videotech" -F "password=91sT7m" -F "upload_token=c4be4441-4647-4327-a420-cb244e3418d3" -F "upload_path=/ftp/android/" -F "files=@$file_name"  --progress-bar`
echo `curl -X POST https://tickets.wemomo.com/api/open/ftp/api/uploadfile/ \-F "secret_key=9094c7970c9b28ce9822af586101410e" -F "username=videotech" -F "password=Cc5Y3V" -F "upload_token=c4be4441-4647-4327-a420-cb244e3418d3" -F "upload_path=/ftp/android/" -F "files=@$file_name"  --progress-bar`
echo "https://download.immomo.com/android/$file_name"
#echo "\ncheck result"
#check_result




