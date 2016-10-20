var smtpDescription = angular.module('ttt.edge.xdr.description', []);

smtpDescription.controller('XdrDescriptionCtrl', ['$scope', '$stateParams',  'XDRTestCasesDescription',
	function($scope, $stateParams, XDRTestCasesDescription) {
		$scope.test_id = $stateParams.id;
		$scope.testObj =  $stateParams.testObj;
		$scope.fieldInput = {};

		XDRTestCasesDescription.getTestCasesDescription(function(response) {
			var result = response.data;

			angular.forEach(result, function(test) {
				if (test.ID === $scope.test_id) {
					$scope.specificTest = test;
				}
			});

		});

		$scope.$watch('specificTest.status', function(value) {
			$scope.laddaLoading = false;
			if (value === 'loading') {
				$scope.laddaLoading = true;
			} else if (value === 'success') {
				$scope.resultDisplay = true;
			} else if (value === 'fail') {
				$scope.resultDisplay = false;
			}
		});
	}
]);
