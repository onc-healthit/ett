var edgeXdr = angular.module('ttt.hisp.xdr', []);

edgeXdr.config(['$stateProvider',
    function($stateProvider) {
        $stateProvider.state('hisp.xdr.main', {
            url: '',
            views: {
                "xdr": {
                    templateUrl: 'hisp/xdr/xdrMain.tpl.html'
                }
            }
        })
        .state('hisp.xdr.description', {
            url: '/description/:id',
            views: {
                "xdr": {
                    templateUrl: 'edge/xdr/description/xdrTestDescription.tpl.html'
                }
            }
        })
        .state('hisp.xdr.logs', {
            url: '/logs',
            views: {
                "xdr": {
                    templateUrl: 'edge/xdr/logs/xdrTestLog.tpl.html'
                }
            }
        });

    }
]);

edgeXdr.controller('HispXdrCtrl', ['$scope', 'XDRTestCasesDescription', 'growl', '$q', '$timeout', 'XDRTestCases', 'XDRCheckStatus',
    function($scope, XDRTestCasesDescription, growl, $q, $timeout, XDRTestCases, XDRCheckStatus) {

        $scope.senderTests = [];
        $scope.receiverTests = [];

        XDRTestCasesDescription.getTestCasesDescription(function(response) {
            var result = response.data;

            angular.forEach(result, function(test) {
                test.status = 'na';
                if (test['SUT: Sender/ Receiver'].toLowerCase().indexOf('sender') >= 0) {
                    $scope.senderTests.push(test);
                } else if (test['SUT: Sender/ Receiver'].toLowerCase().indexOf('receiver') >= 0) {
                    $scope.receiverTests.push(test);
                }
            });

        });

        $scope.displayGrowl = function(text) {
            growl.success(text);
        };

        $scope.$watch('transactionType', function() {
            if ($scope.transactionType === 'sender') {
                $scope.testBench = $scope.senderTests;
            } else {
                $scope.testBench = $scope.receiverTests;
            }
        });

        $scope.runXdr = function(test) {
            test.status = "loading";
            XDRTestCases.save({
                id: test.ID
            }, {
                endpoint: test.endpoint
            }, function(data) {
                test.results = data;
                if (data.status.toLowerCase() === 'error') {
                    test.status = "error";
                } else if ($scope.transactionType === 'sender') {
                    test.status = "pending";
                } else {
                    test.status = "success";
                }
            }, function(data) {
                test.status = 'error';
                throw {
                    code: data.data.code,
                    url: data.data.url,
                    message: data.data.message
                };
            });
        };

        $scope.checkXdrStatus = function(test) {
            test.status = "loading";
            XDRCheckStatus.get({}, {
                id: test.ID
            }, function(data) {
                test.results = data;
                if (data.content === "PASSED") {
                    test.status = "success";
                } else if (data.content === "FAILED") {
                    test.status = "error";
                } else if (data.content === "PENDING") {
                    test.status = "pending";
                }
            }, function(data) {
                test.status = "error";
                throw {
                    code: data.data.code,
                    url: data.data.url,
                    message: data.data.message
                };
            });
        };

    }
]);
