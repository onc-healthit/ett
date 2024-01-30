var directSendV13 = angular.module('ttt.direct.senddirect13', []);

directSendV13.controller('DirectSendV13Ctrl', ['$scope', 'SettingsFactory', 'SendDirect', 'growl', 'LogInfo', 'DirectEmailAddress', 'ApiUrl',
    function($scope, SettingsFactory, SendDirect, growl, LogInfo, DirectEmailAddress, ApiUrl) {

        $scope.apiUrl = ApiUrl.get();

        $scope.message = {};
        $scope.fileInfo = {
            "flowChunkNumber": "",
            "flowChunkSize": "",
            "flowCurrentChunkSize": "",
            "flowTotalSize": "",
            "flowIdentifier": "",
            "flowFilename": "",
            "flowRelativePath": "",
            "flowTotalChunks": ""
        };

        // Get Direct address list for connected user
        // Get username
        $scope.userInfo = LogInfo.getUsername();
        // Asynchronous call so do that when the back end answered with $promise
        $scope.userInfo.$promise.then(function(response) {
            // Get list of direct emails
            if ($scope.userInfo.logged) {
                $scope.directList = DirectEmailAddress.query();
            } else {
                $scope.directList = [];
            }
        });

        // In order to disable the send button when request is pending
        $scope.requestPending = false;
        $scope.sample = {};
        $scope.isWrapped = true;
        $scope.certType = 'GOOD';
        $scope.invalidDigest = false;
        
		$scope.signingCertificate = [
    		{ name: 'GOOD_CERT', val: 'GOOD' },
    		{ name: 'INVALID_CERT', val: 'INVALID' },
    		{ name: 'EXPIRED_CERT', val: 'EXPIRED' },
    		{ name: 'DIFFERENT_TRUST_ANCHOR', val: 'DIFF'  },
    		{ name: 'BAD_AIA', val: 'AIA' },
    		{ name: 'WILD_CARD_DOMAIN_CERT', val: 'WILD_CARD' },
    		{ name: 'CERT_WITH_EMAIL_ADDRESS', val: 'EMAIL' },
    		{ name: 'CERT_LESS_THAN_2048_BITS', val: 'LESS_2048' },
    		{ name: 'CERT_WITH_NO_CRL', val: 'NO_CRL' },
    		{ name: 'CERT_WITH_NO_NOTBEFORE_ATTR', val: 'NO_NOTBEFORE' },
    		{ name: 'CERT_WITH_NO_NOTAFTER_ATT', val: 'NO_NOTAFTER' },
    		{ name: 'CERT_WITH_3072_BITS', val: 'CERT_3072' },
    		{ name: 'CERT_WITH_4096_BITS', val: 'CERT_4096' },
  		];
  		
		$scope.signingAlgorithmNonEcdsa = [
    		{ name: 'SHA-256', val: 'sha256' },
    		{ name: 'SHA-384', val: 'sha384' },
    		{ name: 'SHA-512', val: 'sha512' },
            { name: 'Optimal Asymmetric Encryption Padding (OAEP) for RSA encryption and decryption', val: 'OAEP-RSA' },
    		{ name: 'ECDSA with P-256', val: 'edsap256'  },
    		{ name: 'ECDSA with SHA-256', val: 'edsasha256' },
    		{ name: 'ECDSA with P-384', val: 'edsap384' },
    		{ name: 'ECDSA with SHA-384', val: 'edsasha384' },
    		{ name: 'AES with CBC', val: 'aescbc' },
    		{ name: 'AES with GCM', val: 'aesgcm' },
  		];  

        $scope.signingAlgorithmEcdsa = [
            { name: 'ECDSA with P-256', val: 'edsap256'  },
            { name: 'ECDSA with SHA-256', val: 'edsasha256' },
            { name: 'ECDSA with P-384', val: 'edsap384' },
            { name: 'ECDSA with SHA-384', val: 'edsasha384' }
        ];  

        $scope.signingAlgorithm = $scope.signingAlgorithmNonEcdsa;

        $scope.success = function(message) {
            $scope.fileInfo = angular.fromJson(message);
            $scope.message.CertFilePath = $scope.fileInfo.flowRelativePath;
        };

        $scope.reset = function() {
            $scope.fileInfo = {};
            $scope.message.CertFilePath = undefined;
        };

        $scope.successCcda = function(message) {
            $scope.fileInfo = angular.fromJson(message);
            $scope.message.ownCcdaPath = $scope.fileInfo.flowRelativePath;
        };

        $scope.resetCcda = function() {
            $scope.ownCcda = {};
        };

        $scope.clear = function() {
            $scope.sample.selected = undefined;
        };

        $scope.toggleWrapped = function(bool) {
            $scope.isWrapped = bool;
        };


        $scope.toggleCertType = function(type) {
            if (type === 'INVALID_DIGEST') {
                $scope.certType = '';
                $scope.signingCertificate.selected = undefined;
                $scope.invalidDigest = true;
            }else{
	 			$scope.invalidDigest = false;
                if (type.val === 'GOOD_ECDSA_CERT') {
                    $scope.signingAlgorithm = $scope.signingAlgorithmEcdsa;
                }else{
                    $scope.signingAlgorithm = $scope.signingAlgorithmNonEcdsa;
                }
			}
        };

        $scope.send = function() {
            $scope.laddaLoading = true;
            console.log("Direct 13 save...."+angular.toJson($scope.signingAlgorithm.selected));
            if ($scope.sample.selected === undefined) {
                $scope.message.attachmentFile = "CCDA_Ambulatory.xml";
            } else {
                $scope.message.attachmentFile = $scope.sample.selected.name;
            }

            $scope.fromAddress = $scope.message.fromAddress + '@' + $scope.properties.domainName;
            if ($scope.message.textMessage === null || $scope.message.textMessage === undefined || $scope.message.textMessage === "") {
                $scope.message.textMessage = "Test Message";
            }
            if ($scope.message.subject === null || $scope.message.subject === undefined || $scope.message.subject === "") {
                $scope.message.subject = "Test Message";
            }

            $scope.msgToSend = {
                "textMessage": $scope.message.textMessage,
                "subject": $scope.message.subject,
                "fromAddress": $scope.fromAddress,
                "toAddress": $scope.message.toAddress,
                "attachmentFile": $scope.message.attachmentFile,
                "ownCcdaAttachment": $scope.message.ownCcdaPath,
                "signingCert": $scope.signingCertificate.selected ? $scope.signingCertificate.selected.val : '',
                "signingCertPassword": "",
                "encryptionCert": $scope.message.CertFilePath,
                "wrapped": $scope.isWrapped,
                "invalidDigest": $scope.invalidDigest || false,
                "digestAlgo": $scope.signingAlgorithm.selected ? $scope.signingAlgorithm.selected.val : 'sha256',
                "directVersion": "v13"
            };

            SendDirect.save($scope.msgToSend, function(data) {
                $scope.laddaLoading = false;
                if (data.result) {
                    growl.success("Message successfully sent!");
                } else {
                    throw {
                        code: '0x0004',
                        message: "Cannot send the message"
                    };
                }
            }, function(data) {
                $scope.laddaLoading = false;
                throw {
                    code: data.data.code,
                    url: data.data.url,
                    message: data.data.message
                };
            });

        };

    }
]);
