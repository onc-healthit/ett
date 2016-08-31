var xdmValidator = angular.module('ttt.direct.xdrValidator', []);

ccdaValidator.controller('XDRValidatorCtrl', ['$scope', 'growl', 'XDRValidatorEndpoints',
    function($scope, growl, XDRValidatorEndpoints) {

        $scope.xdrSamples = [
            {name:"C32_Sample1_full_metadata"},
            {name:"C32_Sample1_minimal_metadata"},
            {name:"C32_Sample2_full_metadata"},
            {name:"C32_Sample2_minimal_metadata"},
            {name:"CCDA_Ambulatory_full_metadata"},
            {name:"CCDA_Ambulatory_minimal_metadata"},
            {name:"CCDA_Inpatient_full_metadata"},
            {name:"CCDA_Inpatient_minimal_metadata"},
            {name:"CCR_Sample1_full_metadata"},
            {name:"CCR_Sample1_minimal_metadata"},
            {name:"CCR_Sample2_full_metadata"},
            {name:"CCR_Sample2_minimal_metadata"}
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
