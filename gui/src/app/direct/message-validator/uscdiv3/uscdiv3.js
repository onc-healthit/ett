var ccdaValidator = angular.module('ttt.direct.USCDIV3Validator', []);

ccdaValidator.controller('USCDIV3ValidatorCtrl', ['$scope', 'USCDIV3ValidatorFactory', '$state', 'ApiUrl', '$filter','CCDADocumentService','$location','$anchorScroll',
    function($scope, USCDIV3ValidatorFactory, $state, ApiUrl, $filter,CCDADocumentService,$location,$anchorScroll) {

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


        $scope.fileInfoCdaIg = {
            "flowChunkNumber": "",
            "flowChunkSize": "",
            "flowCurrentChunkSize": "",
            "flowTotalSize": "",
            "flowIdentifier": "",
            "flowFilename": "",
            "flowRelativePath": "",
            "flowTotalChunks": ""
        };
        $scope.sutRole = "Receiver";

        $scope.senderreceiver ="Sender";

        $scope.objective = [];

        $scope.filename = [];

        $scope.ccdaData = {};


        $scope.ccdaSenderData = {};


        $scope.ccdatype = "Uscdiv3";
  
        $scope.ccdaParams = {
            "ccdatype": $scope.ccdatype,
            "sutrole": $scope.sutRole ,
            "filename": ''  
        };

        $scope.switchDocType = function(type) {
            console.log("before switch $scope.ccdaParams.sutrole :::::"+$scope.ccdaParams.sutrole);
            $scope.ccdaParams.filename = '';
            $scope.ccdaParams.sutrole =type;
            $scope.senderreceiver = type;
            console.log("$scope.ccdaParams.sutrole :::::"+$scope.ccdaParams.sutrole);
            CCDADocumentService.get($scope.ccdaParams , function(data) {
              $scope.ccdaDocuments = data;
                if (data !== null) {
                    $scope.ccdaSelectData = JSON.parse($scope.ccdaDocuments['ccdadata']);
                }
            });            

       };

        $scope.changed = function(item) {
            $scope.ccdaParams.filename = item.name ;
           console.log("$scope.ccdaParams.sutrole for file name:::::"+$scope.ccdaParams.sutrole);
            CCDADocumentService.get($scope.ccdaParams , function(data) {
                if (data !== null) {
                    $scope.ccdaFileNames = JSON.parse(data['ccdadata']);
                }
            });
            $scope.type = item;
            $scope.ccdaDocument = "";
            $scope.filename.selected ="";
        };

        $scope.setFileName = function(item) {
            $scope.ccdaDocument = item;
        };

        $scope.apiUrl = ApiUrl.get();

        $scope.validator = {
            "messageFilePath": "",
            "validationObjective": "",
            "referenceFileName": "",
            "messageFileCdaIg": "",
            "messageFile": ""
        };

        $scope.successMessage = function(message) {
            $scope.uploadCcda ="true";
            $scope.fileInfo = angular.fromJson(message);
            $scope.validator.messageFile = $scope.fileInfo.flowRelativePath;
        };

        $scope.resetMessage = function() {
            $scope.uploadCcda =undefined;
             $scope.validator.messageFile ="";
        };

        $scope.successMessageCdaIg = function(messageCdaIg) {
            $scope.uploadCdaIg ="true";
            $scope.fileInfoCdaIg = angular.fromJson(messageCdaIg);
            $scope.validator.messageFileCdaIg = $scope.fileInfoCdaIg.flowRelativePath;
        };

        $scope.resetMessageCdaIg = function() {
            $scope.uploadCdaIg =undefined;
            $scope.validator.messageFileCdaIg ="";
        };


$scope.gotodiv = function(anchor) {
    $location.hash(anchor);
   // call $anchorScroll()
    $anchorScroll();
};
        $scope.validateCdaIg = function() {
            $scope.laddaLoadingCdaIg = true;
            if ($scope.uploadCdaIg!==undefined){
                $scope.validator.messageFilePath = $scope.validator.messageFileCdaIg;
                    $scope.validator.validationObjective = 'Readme.txt';
                    $scope.validator.referenceFileName = 'CDA_IG_Plus_Vocab';
                    USCDIV3ValidatorFactory.save($scope.validator, function(data) {
                        $scope.laddaLoadingCdaIg = false;
                        $scope.ccdaappendfilename =    {ccdafilenaame : $scope.validator.referenceFileName};
                        $scope.ccdaResult = angular.extend(data, $scope.ccdaappendfilename);
                        $scope.gotodiv("ccdaValdReport");
                    }, function(data) {
                        $scope.laddaLoadingCdaIg = false;
                        throw {
                            code: data.data.code,
                            url: data.data.url,
                            message: data.data.message
                        };
                    });
            }else{
                    $scope.laddaLoadingCdaIg = false;
                    throw {
                        code: "No code",
                        url: "",
                        message: "No C-CDA attachment uploaded "
                    };
                }
        };

        $scope.validate = function() {
            $scope.laddaLoading = true;
            if ($scope.uploadCcda !==undefined ){
                $scope.validator.messageFilePath = $scope.validator.messageFile;
                if ($scope.ccdaDocument) {
                    if ($scope.ccdaDocument.name && $scope.ccdaDocument.path) {
                        console.log("before switch $scope.ccdaParams.filename :::::"+$scope.ccdaParams.filename);             
                        $scope.validator.validationObjective = $scope.ccdaParams.filename;
                        console.log("before switch validationObjective :::::"+$scope.validator.validationObjective );
                        $scope.validator.referenceFileName = $scope.ccdaDocument.name;
                        USCDIV3ValidatorFactory.save($scope.validator, function(data) {
                            $scope.laddaLoading = false;
                            $scope.ccdaappendfilename =    {ccdafilenaame : $scope.validator.referenceFileName};
                            $scope.ccdaResult = angular.extend(data, $scope.ccdaappendfilename);
                            $scope.gotodiv("ccdaValdReport");
                        }, function(data) {
                            $scope.laddaLoading = false;
                            throw {
                                code: data.data.code,
                                url: data.data.url,
                                message: data.data.message
                            };
                        });
                    } else {
                        $scope.laddaLoading = false;
                        throw {
                            code: "No code",
                            url: "",
                            message: "You need to select a C-CDA document name"
                        };
                    }
                } else {
                    $scope.laddaLoading = false;
                    throw {
                        code: "No code",
                        url: "",
                        message: "You need to select a C-CDA document type"
                    };
                }
            }else{
                    $scope.laddaLoading = false;
                    throw {
                        code: "No code",
                        url: "",
                        message: "No C-CDA attachment uploaded "
                    };
                }
        };

        $scope.$watch('sutRole', function(newV, oldV) {

        });

    }
]);