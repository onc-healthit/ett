var announcementCtrl = angular.module('ttt.announcement', []);

announcementCtrl.controller('AnnouncementCtrl', ['$scope','$http', '$sce',
    function($scope,$http, $sce) {
             $scope.accouncments = "";
             $http({
                    method: 'GET',
                    url: 'api/announcement',
                    data: {},
                    transformResponse: function (data, headersGetter, status) {
                                       $scope.accouncments = data;
                                       return {data: data};
                     }
                     }).success(function () {
                             //Some success function
                     }).error(function () {
                             //Some error function
            });
         $scope.renderHtml = function (htmlCode) {
              return $sce.trustAsHtml(markdown.toHTML(htmlCode));
         };
    }
]);
