var xdmValidator = angular.module('ttt.direct.xdrValidator', []);

ccdaValidator.controller('XDRValidatorCtrl', ['$scope', 'growl', 'XDRValidatorEndpoints',
    function($scope, growl, XDRValidatorEndpoints) {

        $scope.xdrSamples = [
            "C32_Sample1_full_metadata",
            "C32_Sample1_minimal_metadata",
            "C32_Sample2_full_metadata",
            "C32_Sample2_minimal_metadata",
            "CCDA_Ambulatory_full_metadata",
            "CCDA_Ambulatory_minimal_metadata",
            "CCDA_Inpatient_full_metadata",
            "CCDA_Inpatient_minimal_metadata",
            "CCR_Sample1_full_metadata",
            "CCR_Sample1_minimal_metadata",
            "CCR_Sample2_full_metadata",
            "CCR_Sample2_minimal_metadata"
        ];

        $scope.sample = {};
        $scope.test = {};

        XDRValidatorEndpoints.get(function(data) {
            if (data.content) {
                $scope.test.endpoint = data.content.value.endpoint;
                $scope.test.endpointTLS = data.content.value.endpointTLS;
            }
        });

        $scope.displayGrowl = function(text) {
            growl.success(text);
        };
    }
]);
