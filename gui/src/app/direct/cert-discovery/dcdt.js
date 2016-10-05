var dcdtValidator = angular.module('ttt.direct.dcdtValidator', []);

dcdtValidator.controller('DCDTValidatorCtrl', ['$scope', '$state', 'ApiUrl',
    function($scope, $state, ApiUrl) {

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


    }
]);
