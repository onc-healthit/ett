var dcdtValidator = angular.module('ttt.direct.dcdtValidator', []);

dcdtValidator.controller('DCDTValidatorCtrl', ['$scope', 'DCDTValidatorFactory', '$state', 'ApiUrl','$http','CCDADocumentsFactory','$timeout', 'growl',
	function($scope, DCDTValidatorFactory, $state, ApiUrl,$http,CCDADocumentsFactory,$timeout ,growl) {
	$scope.pageTitle= $state.current.data.pageTitle;
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
		$scope.alerts = [];
		$scope.discalerts = [];
    $scope.data = [
      {name:"Hosting allows a System Under Test (SUT) to verify that their certificates are hosted correctly, and discoverable by other Direct implementations.", hreflink:"panel_hosting",children:0},
      {name:"Discovery allows a SUT to verify that they can discover certificates in other Direct implementations by using them to send Direct messages.",  hreflink:"panel_discovery",children:1}
    ];

 $scope.directAddress ="";
 $scope.testcase="";
 $scope.discEmailAddr ="";
 $scope.discResultEmailAddr="";
$scope.discoveryTestCase = [
    { code: "", name: "--No testcase selected--" },
    { code: "D1_DNS_AB_Valid", name: "D1 - Valid address-bound certificate discovery in DNS",
      Negative: "false",
      Optional: "false",
      Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
      RTM_Sections: "1, 3",
      RFC_4398:  "Section 2.1",
      Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
      Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
      Target_Certificate: [{"name": "D1_valA",
                            "Valid": "true",
                            "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                            "Binding_Type": "ADDRESS",
                            "Locaton": [{"Type": "DNS",
                                 "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                             }]
                         }],
      Background_Certificate: [{"name": "D1_invB",
          "Valid": "false",
          "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
          "Binding_Type": "DOMAIN",
          "Locaton": [{"Type": "DNS",
               "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
           }]
       },
       {"name": "D1_invC",
           "Valid": "false",
           "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
           "Binding_Type": "ADDRESS",
           "Locaton": [{"Type": "DNS",
                "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                "Host":"0.0.0.0",
                "Port":"10389"
            }]
        },
        {"name": "D1_invD",
            "Valid": "false",
            "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
            "Binding_Type": "DOMAIN",
            "Locaton": [{"Type": "DNS",
                 "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                     "Host":"0.0.0.0",
                     "Port":"10389"            }]
         }
         ]
     },
     { code: "D2_DNS_DB_Valid", name: "D2 - Valid domain-bound certificate discovery in DNS",
         Negative: "false",
         Optional: "false",
         Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
         RTM_Sections: "1, 3",
         RFC_4398:  "Section 2.1",
         Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
         Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
         Target_Certificate: [{"name": "D2_valB",
                               "Valid": "true",
                               "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                               "Binding_Type": "DOMAIN",
                               "Locaton": [{"Type": "DNS",
                                    "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                }]
                            }],
         Background_Certificate: [
          {"name": "D2_invC",
              "Valid": "false",
              "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
              "Binding_Type": "ADDRESS",
              "Locaton": [{"Type": "DNS",
                   "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                   "Host":"0.0.0.0",
                   "Port":"10389"
               }]
           },
           {"name": "D1_invD",
               "Valid": "false",
               "Description": " Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
               "Binding_Type": "DOMAIN",
               "Locaton": [{"Type": "DNS",
                    "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                        "Host":"0.0.0.0",
                        "Port":"10389"            }]
            }
            ]
        },
        { code: "D3_LDAP_AB_Valid", name: "D3 - Valid address-bound certificate discovery in LDAP",
            Negative: "false",
            Optional: "false",
            Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
            RTM_Sections: "1, 3",
            RFC_4398:  "Section 2.1",
            Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
            Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
            Target_Certificate: [{"name": "D1_valA",
                                  "Valid": "true",
                                  "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                  "Binding_Type": "ADDRESS",
                                  "Locaton": [{"Type": "DNS",
                                       "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                   }]
                               }],
            Background_Certificate: [{"name": "D1_invB",
                "Valid": "false",
                "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                "Binding_Type": "DOMAIN",
                "Locaton": [{"Type": "DNS",
                     "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                 }]
             },
             {"name": "D1_invC",
                 "Valid": "false",
                 "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                 "Binding_Type": "ADDRESS",
                 "Locaton": [{"Type": "DNS",
                      "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                      "Host":"0.0.0.0",
                      "Port":"10389"
                  }]
              },
              {"name": "D1_invD",
                  "Valid": "false",
                  "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                  "Binding_Type": "DOMAIN",
                  "Locaton": [{"Type": "DNS",
                       "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                           "Host":"0.0.0.0",
                           "Port":"10389"            }]
               }
               ]
           },
           { code: "D4_LDAP_DB_Valid", name: "D4 - Valid domain-bound certificate discovery in LDAP",
               Negative: "false",
               Optional: "false",
               Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
               RTM_Sections: "1, 3",
               RFC_4398:  "Section 2.1",
               Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
               Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
               Target_Certificate: [{"name": "D1_valA",
                                     "Valid": "true",
                                     "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                     "Binding_Type": "ADDRESS",
                                     "Locaton": [{"Type": "DNS",
                                          "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                      }]
                                  }],
               Background_Certificate: [{"name": "D1_invB",
                   "Valid": "false",
                   "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                   "Binding_Type": "DOMAIN",
                   "Locaton": [{"Type": "DNS",
                        "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                    }]
                },
                {"name": "D1_invC",
                    "Valid": "false",
                    "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                    "Binding_Type": "ADDRESS",
                    "Locaton": [{"Type": "DNS",
                         "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                         "Host":"0.0.0.0",
                         "Port":"10389"
                     }]
                 },
                 {"name": "D1_invD",
                     "Valid": "false",
                     "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                     "Binding_Type": "DOMAIN",
                     "Locaton": [{"Type": "DNS",
                          "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                              "Host":"0.0.0.0",
                              "Port":"10389"            }]
                  }
                  ]
              },
              { code: "D5_DNS_AB_Invalid", name: "D5 - Invalid address-bound certificate discovery in DNS",
                  Negative: "false",
                  Optional: "false",
                  Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
                  RTM_Sections: "1, 3",
                  RFC_4398:  "Section 2.1",
                  Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
                  Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
                  Target_Certificate: [{"name": "D1_valA",
                                        "Valid": "true",
                                        "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                        "Binding_Type": "ADDRESS",
                                        "Locaton": [{"Type": "DNS",
                                             "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                         }]
                                     }],
                  Background_Certificate: [{"name": "D1_invB",
                      "Valid": "false",
                      "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                      "Binding_Type": "DOMAIN",
                      "Locaton": [{"Type": "DNS",
                           "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                       }]
                   },
                   {"name": "D1_invC",
                       "Valid": "false",
                       "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                       "Binding_Type": "ADDRESS",
                       "Locaton": [{"Type": "DNS",
                            "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                            "Host":"0.0.0.0",
                            "Port":"10389"
                        }]
                    },
                    {"name": "D1_invD",
                        "Valid": "false",
                        "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                        "Binding_Type": "DOMAIN",
                        "Locaton": [{"Type": "DNS",
                             "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                 "Host":"0.0.0.0",
                                 "Port":"10389"            }]
                     }
                     ]
                 },
                 { code: "D6_DNS_DB_Invalid", name: "D6 - Invalid domain-bound certificate discovery in DNS",
                     Negative: "false",
                     Optional: "false",
                     Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
                     RTM_Sections: "1, 3",
                     RFC_4398:  "Section 2.1",
                     Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
                     Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
                     Target_Certificate: [{"name": "D1_valA",
                                           "Valid": "true",
                                           "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                           "Binding_Type": "ADDRESS",
                                           "Locaton": [{"Type": "DNS",
                                                "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                            }]
                                        }],
                     Background_Certificate: [{"name": "D1_invB",
                         "Valid": "false",
                         "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                         "Binding_Type": "DOMAIN",
                         "Locaton": [{"Type": "DNS",
                              "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                          }]
                      },
                      {"name": "D1_invC",
                          "Valid": "false",
                          "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                          "Binding_Type": "ADDRESS",
                          "Locaton": [{"Type": "DNS",
                               "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                               "Host":"0.0.0.0",
                               "Port":"10389"
                           }]
                       },
                       {"name": "D1_invD",
                           "Valid": "false",
                           "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                           "Binding_Type": "DOMAIN",
                           "Locaton": [{"Type": "DNS",
                                "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                    "Host":"0.0.0.0",
                                    "Port":"10389"            }]
                        }
                        ]
                    },
                { code: "D7_LDAP_AB_Invalid", name: "D7 - Invalid address-bound certificate discovery in LDAP",
                    Negative: "false",
                    Optional: "false",
                    Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
                    RTM_Sections: "1, 3",
                    RFC_4398:  "Section 2.1",
                    Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
                    Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
                    Target_Certificate: [{"name": "D1_valA",
                                          "Valid": "true",
                                          "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                          "Binding_Type": "ADDRESS",
                                          "Locaton": [{"Type": "DNS",
                                               "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                           }]
                                       }],
                    Background_Certificate: [{"name": "D1_invB",
                        "Valid": "false",
                        "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                        "Binding_Type": "DOMAIN",
                        "Locaton": [{"Type": "DNS",
                             "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                         }]
                     },
                     {"name": "D1_invC",
                         "Valid": "false",
                         "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                         "Binding_Type": "ADDRESS",
                         "Locaton": [{"Type": "DNS",
                              "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                              "Host":"0.0.0.0",
                              "Port":"10389"
                          }]
                      },
                      {"name": "D1_invD",
                          "Valid": "false",
                          "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                          "Binding_Type": "DOMAIN",
                          "Locaton": [{"Type": "DNS",
                               "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                   "Host":"0.0.0.0",
                                   "Port":"10389"            }]
                       }
                       ]
                   },
                   { code: "D8_LDAP_DB_Invalid", name: "D8 - Invalid domain-bound certificate discovery in LDAP",
                       Negative: "false",
                       Optional: "false",
                       Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
                       RTM_Sections: "1, 3",
                       RFC_4398:  "Section 2.1",
                       Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
                       Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
                       Target_Certificate: [{"name": "D1_valA",
                                             "Valid": "true",
                                             "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                             "Binding_Type": "ADDRESS",
                                             "Locaton": [{"Type": "DNS",
                                                  "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                              }]
                                          }],
                       Background_Certificate: [{"name": "D1_invB",
                           "Valid": "false",
                           "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                           "Binding_Type": "DOMAIN",
                           "Locaton": [{"Type": "DNS",
                                "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                            }]
                        },
                        {"name": "D1_invC",
                            "Valid": "false",
                            "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                            "Binding_Type": "ADDRESS",
                            "Locaton": [{"Type": "DNS",
                                 "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                 "Host":"0.0.0.0",
                                 "Port":"10389"
                             }]
                         },
                         {"name": "D1_invD",
                             "Valid": "false",
                             "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                             "Binding_Type": "DOMAIN",
                             "Locaton": [{"Type": "DNS",
                                  "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                      "Host":"0.0.0.0",
                                      "Port":"10389"            }]
                          }
                          ]
                      },
                      { code: "D9_DNS_AB_SelectValid", name: "D9 - Select valid address-bound certificate over invalid certificate in DNS",
                          Negative: "false",
                          Optional: "false",
                          Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
                          RTM_Sections: "1, 3",
                          RFC_4398:  "Section 2.1",
                          Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
                          Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
                          Target_Certificate: [{"name": "D1_valA",
                                                "Valid": "true",
                                                "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                                "Binding_Type": "ADDRESS",
                                                "Locaton": [{"Type": "DNS",
                                                     "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                                 }]
                                             }],
                          Background_Certificate: [{"name": "D1_invB",
                              "Valid": "false",
                              "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                              "Binding_Type": "DOMAIN",
                              "Locaton": [{"Type": "DNS",
                                   "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                               }]
                           },
                           {"name": "D1_invC",
                               "Valid": "false",
                               "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                               "Binding_Type": "ADDRESS",
                               "Locaton": [{"Type": "DNS",
                                    "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                    "Host":"0.0.0.0",
                                    "Port":"10389"
                                }]
                            },
                            {"name": "D1_invD",
                                "Valid": "false",
                                "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                "Binding_Type": "DOMAIN",
                                "Locaton": [{"Type": "DNS",
                                     "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                         "Host":"0.0.0.0",
                                         "Port":"10389"            }]
                             }
                             ]
                         },
                         { code: "D10_LDAP_AB_UnavailableLDAPServer", name: "D10 - Certificate discovery in LDAP with one unavailable LDAP server",
                             Negative: "false",
                             Optional: "false",
                             Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
                             RTM_Sections: "1, 3",
                             RFC_4398:  "Section 2.1",
                             Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
                             Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
                             Target_Certificate: [{"name": "D1_valA",
                                                   "Valid": "true",
                                                   "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                                   "Binding_Type": "ADDRESS",
                                                   "Locaton": [{"Type": "DNS",
                                                        "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                                    }]
                                                }],
                             Background_Certificate: [{"name": "D1_invB",
                                 "Valid": "false",
                                 "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                 "Binding_Type": "DOMAIN",
                                 "Locaton": [{"Type": "DNS",
                                      "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                  }]
                              },
                              {"name": "D1_invC",
                                  "Valid": "false",
                                  "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                                  "Binding_Type": "ADDRESS",
                                  "Locaton": [{"Type": "DNS",
                                       "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                       "Host":"0.0.0.0",
                                       "Port":"10389"
                                   }]
                               },
                               {"name": "D1_invD",
                                   "Valid": "false",
                                   "Description": " Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                   "Binding_Type": "DOMAIN",
                                   "Locaton": [{"Type": "DNS",
                                        "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                            "Host":"0.0.0.0",
                                            "Port":"10389"            }]
                                }
                                ]
                            },
                            { code: "D11_DNS_NB_NoDNSCertsorSRV", name: "D11 - No certificates discovered in DNS CERT records and no SRV records",
                                Negative: "false",
                                Optional: "false",
                                Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
                                RTM_Sections: "1, 3",
                                RFC_4398:  "Section 2.1",
                                Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
                                Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
                                Target_Certificate: [{"name": "D1_valA",
                                                      "Valid": "true",
                                                      "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                                      "Binding_Type": "ADDRESS",
                                                      "Locaton": [{"Type": "DNS",
                                                           "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                                       }]
                                                   }],
                                Background_Certificate: [{"name": "D1_invB",
                                    "Valid": "false",
                                    "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                    "Binding_Type": "DOMAIN",
                                    "Locaton": [{"Type": "DNS",
                                         "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                     }]
                                 },
                                 {"name": "D1_invC",
                                     "Valid": "false",
                                     "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                                     "Binding_Type": "ADDRESS",
                                     "Locaton": [{"Type": "DNS",
                                          "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                          "Host":"0.0.0.0",
                                          "Port":"10389"
                                      }]
                                  },
                                  {"name": "D1_invD",
                                      "Valid": "false",
                                      "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                      "Binding_Type": "DOMAIN",
                                      "Locaton": [{"Type": "DNS",
                                           "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                               "Host":"0.0.0.0",
                                               "Port":"10389"            }]
                                   }
                                   ]
                               },
                               { code: "D12_LDAP_NB_UnavailableLDAPServer", name: "D12 - No certificates found in DNS CERT records and no available LDAP servers",
                                   Negative: "false",
                                   Optional: "false",
                                   Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
                                   RTM_Sections: "1, 3",
                                   RFC_4398:  "Section 2.1",
                                   Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
                                   Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
                                   Target_Certificate: [{"name": "D1_valA",
                                                         "Valid": "true",
                                                         "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                                         "Binding_Type": "ADDRESS",
                                                         "Locaton": [{"Type": "DNS",
                                                              "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                                          }]
                                                      }],
                                   Background_Certificate: [{"name": "D1_invB",
                                       "Valid": "false",
                                       "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                       "Binding_Type": "DOMAIN",
                                       "Locaton": [{"Type": "DNS",
                                            "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                        }]
                                    },
                                    {"name": "D1_invC",
                                        "Valid": "false",
                                        "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                                        "Binding_Type": "ADDRESS",
                                        "Locaton": [{"Type": "DNS",
                                             "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                             "Host":"0.0.0.0",
                                             "Port":"10389"
                                         }]
                                     },
                                     {"name": "D1_invD",
                                         "Valid": "false",
                                         "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                         "Binding_Type": "DOMAIN",
                                         "Locaton": [{"Type": "DNS",
                                              "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                                  "Host":"0.0.0.0",
                                                  "Port":"10389"            }]
                                      }
                                      ]
                                  },
                                  { code: "D13_LDAP_NB_NoCerts", name: "D13 - No certificates discovered in DNS CERT records or LDAP servers",
                                      Negative: "false",
                                      Optional: "false",
                                      Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
                                      RTM_Sections: "1, 3",
                                      RFC_4398:  "Section 2.1",
                                      Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
                                      Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
                                      Target_Certificate: [{"name": "D1_valA",
                                                            "Valid": "true",
                                                            "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                                            "Binding_Type": "ADDRESS",
                                                            "Locaton": [{"Type": "DNS",
                                                                 "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                                             }]
                                                         }],
                                      Background_Certificate: [{"name": "D1_invB",
                                          "Valid": "false",
                                          "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                          "Binding_Type": "DOMAIN",
                                          "Locaton": [{"Type": "DNS",
                                               "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                           }]
                                       },
                                       {"name": "D1_invC",
                                           "Valid": "false",
                                           "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                                           "Binding_Type": "ADDRESS",
                                           "Locaton": [{"Type": "DNS",
                                                "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                                "Host":"0.0.0.0",
                                                "Port":"10389"
                                            }]
                                        },
                                        {"name": "D1_invD",
                                            "Valid": "false",
                                            "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                            "Binding_Type": "DOMAIN",
                                            "Locaton": [{"Type": "DNS",
                                                 "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                                     "Host":"0.0.0.0",
                                                     "Port":"10389"            }]
                                         }
                                         ]
                                     },
                                     { code: "D14_DNS_AB_TCPLargeCert", name: "D14 - Discovery of certificate larger than 512 bytes in DNS",
                                         Negative: "false",
                                         Optional: "false",
                                         Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
                                         RTM_Sections: "1, 3",
                                         RFC_4398:  "Section 2.1",
                                         Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
                                         Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
                                         Target_Certificate: [{"name": "D1_valA",
                                                               "Valid": "true",
                                                               "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                                               "Binding_Type": "ADDRESS",
                                                               "Locaton": [{"Type": "DNS",
                                                                    "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                                                }]
                                                            }],
                                         Background_Certificate: [{"name": "D1_invB",
                                             "Valid": "false",
                                             "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                             "Binding_Type": "DOMAIN",
                                             "Locaton": [{"Type": "DNS",
                                                  "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                              }]
                                          },
                                          {"name": "D1_invC",
                                              "Valid": "false",
                                              "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                                              "Binding_Type": "ADDRESS",
                                              "Locaton": [{"Type": "DNS",
                                                   "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                                   "Host":"0.0.0.0",
                                                   "Port":"10389"
                                               }]
                                           },
                                           {"name": "D1_invD",
                                               "Valid": "false",
                                               "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                               "Binding_Type": "DOMAIN",
                                               "Locaton": [{"Type": "DNS",
                                                    "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                                        "Host":"0.0.0.0",
                                                        "Port":"10389"            }]
                                            }
                                            ]
                                        },
                                    { code: "D15_LDAP_AB_SRVPriority", name: "D15 - Certificate discovery in LDAP based on SRV priority value",
                                        Negative: "false",
                                        Optional: "false",
                                        Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
                                        RTM_Sections: "1, 3",
                                        RFC_4398:  "Section 2.1",
                                        Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
                                        Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
                                        Target_Certificate: [{"name": "D1_valA",
                                                              "Valid": "true",
                                                              "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                                              "Binding_Type": "ADDRESS",
                                                              "Locaton": [{"Type": "DNS",
                                                                   "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                                               }]
                                                           }],
                                        Background_Certificate: [{"name": "D1_invB",
                                            "Valid": "false",
                                            "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                            "Binding_Type": "DOMAIN",
                                            "Locaton": [{"Type": "DNS",
                                                 "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                             }]
                                         },
                                         {"name": "D1_invC",
                                             "Valid": "false",
                                             "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                                             "Binding_Type": "ADDRESS",
                                             "Locaton": [{"Type": "DNS",
                                                  "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                                  "Host":"0.0.0.0",
                                                  "Port":"10389"
                                              }]
                                          },
                                          {"name": "D1_invD",
                                              "Valid": "false",
                                              "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                              "Binding_Type": "DOMAIN",
                                              "Locaton": [{"Type": "DNS",
                                                   "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                                       "Host":"0.0.0.0",
                                                       "Port":"10389"            }]
                                           }
                                           ]
                                       },
                                       { code: "D16_LDAP_AB_SRVWeight", name: "D16 - Certificate discovery in LDAP based on SRV weight value",
                                           Negative: "false",
                                           Optional: "false",
                                           Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
                                           RTM_Sections: "1, 3",
                                           RFC_4398:  "Section 2.1",
                                           Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
                                           Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
                                           Target_Certificate: [{"name": "D1_valA",
                                                                 "Valid": "true",
                                                                 "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                                                 "Binding_Type": "ADDRESS",
                                                                 "Locaton": [{"Type": "DNS",
                                                                      "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                                                  }]
                                                              }],
                                           Background_Certificate: [{"name": "D1_invB",
                                               "Valid": "false",
                                               "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                               "Binding_Type": "DOMAIN",
                                               "Locaton": [{"Type": "DNS",
                                                    "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                                }]
                                            },
                                            {"name": "D1_invC",
                                                "Valid": "false",
                                                "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                                                "Binding_Type": "ADDRESS",
                                                "Locaton": [{"Type": "DNS",
                                                     "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                                     "Host":"0.0.0.0",
                                                     "Port":"10389"
                                                 }]
                                             },
                                             {"name": "D1_invD",
                                                 "Valid": "false",
                                                 "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                                 "Binding_Type": "DOMAIN",
                                                 "Locaton": [{"Type": "DNS",
                                                      "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                                          "Host":"0.0.0.0",
                                                          "Port":"10389"            }]
                                              }
                                              ]
                                          },
                                          { code: "D17_DNS_AB_CRLRevocation", name: "D17 - CRL-based revocation checking for address-bound certificate discovery in DNS",
                                              Negative: "false",
                                              Optional: "false",
                                              Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
                                              RTM_Sections: "1, 3",
                                              RFC_4398:  "Section 2.1",
                                              Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
                                              Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
                                              Target_Certificate: [{"name": "D1_valA",
                                                                    "Valid": "true",
                                                                    "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                                                    "Binding_Type": "ADDRESS",
                                                                    "Locaton": [{"Type": "DNS",
                                                                         "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                                                     }]
                                                                 }],
                                              Background_Certificate: [{"name": "D1_invB",
                                                  "Valid": "false",
                                                  "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                                  "Binding_Type": "DOMAIN",
                                                  "Locaton": [{"Type": "DNS",
                                                       "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                                   }]
                                               },
                                               {"name": "D1_invC",
                                                   "Valid": "false",
                                                   "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                                                   "Binding_Type": "ADDRESS",
                                                   "Locaton": [{"Type": "DNS",
                                                        "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                                        "Host":"0.0.0.0",
                                                        "Port":"10389"
                                                    }]
                                                },
                                                {"name": "D1_invD",
                                                    "Valid": "false",
                                                    "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                                                    "Binding_Type": "DOMAIN",
                                                    "Locaton": [{"Type": "DNS",
                                                         "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                                                             "Host":"0.0.0.0",
                                                             "Port":"10389"            }]
                                                 }
                                                 ]
                                             },           { code: "D18_DNS_AB_AIAIntermediateIssuer", name: "D18 - AIA-based intermediate issuer certificate retrieval for address-bound certificate discovery in DNS",
            Negative: "false",
            Optional: "false",
            Description: "This test case verifies that your system can query DNS for address-bound CERT records and discover a valid address-bound X.509 certificate for a Direct address.",
            RTM_Sections: "1, 3",
            RFC_4398:  "Section 2.1",
            Direct_SHT: "Direct Applicability Statement for Secure Health Transport: Sections 4.0 and 5.3",
            Instructions: "You should have received an email indicating the test case results for your system. Examine the results to see if your system passed the test case. If you do not receive a message for the test case, then you should assume that the test case failed.",
            Target_Certificate: [{"name": "D1_valA",
                                  "Valid": "true",
                                  "Description": "Valid address-bound certificate in a DNS CERT record containing the Direct address in the rfc822Name of the SubjectAlternativeName extension.",
                                  "Binding_Type": "ADDRESS",
                                  "Locaton": [{"Type": "DNS",
                                       "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                                   }]
                               }],
            Background_Certificate: [{"name": "D1_invB",
                "Valid": "false",
                "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                "Binding_Type": "DOMAIN",
                "Locaton": [{"Type": "DNS",
                     "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org"
                 }]
             },
             {"name": "D1_invC",
                 "Valid": "false",
                 "Description": "Invalid address-bound certificate for the Direct address in an LDAP server with an associated SRV record.",
                 "Binding_Type": "ADDRESS",
                 "Locaton": [{"Type": "DNS",
                      "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                      "Host":"0.0.0.0",
                      "Port":"10389"
                  }]
              },
              {"name": "D1_invD",
                  "Valid": "false",
                  "Description": "Invalid domain-bound certificate for the Direct address in a DNS CERT record.",
                  "Binding_Type": "DOMAIN",
                  "Locaton": [{"Type": "DNS",
                       "Mail_Addres": "d1@domain1.dcdt30prod.sitenv.org",
                           "Host":"0.0.0.0",
                           "Port":"10389"            }]
               }
               ]
           }];


$scope.processes= [
    { code: "", name: "--No testcase selected--" },
    { code: "H1_DNS_AB_Normal", name: "H1 - Normal address-bound certificate search in DNS",
Binding_Type: "ADDRESS",
Location_Type: "DNS",
Negative: "false",
Optional: "false",
Description: "This test case verifies that your system's DNS can host and return the expected address-bound X.509 certificate.",
RTM_Sections: "1, 3",
RFC_4398:  "Section 2.1",
Direct_SHT: "Section 5.3",
Instructions: "Enter a Direct address corresponding to an address-bound X.509 certificate that is hosted by your system's DNS and then click Submit. DCDT will attempt to discover the certificate and display the result on the screen."
},
{ code: "H2_DNS_DB_Normal", name: "H2 - Normal domain-bound certificate search in DNS",
Binding_Type: "DOMAIN",
Location_Type: "DNS",
Negative: "false",
Optional: "false",
Description: "This test case verifies that your system's DNS can host and return the expected domain-bound X.509 certificate.",
RTM_Sections: "1, 3",
RFC_4398:  "Section 2.1",
Direct_SHT: "Section 5.3",
Instructions: "Enter a Direct address corresponding to a domain-bound X.509 certificate that is hosted by your system's DNS and then click Submit. DCDT will attempt to discover the certificate and display the result on the screen."
},
{ code: "H3_LDAP_AB_Normal", name: "H3 - Normal address-bound certificate search in LDAP",
Binding_Type: "ADDRESS",
Location_Type: "LDAP",
Negative: "false",
Optional: "false",
Description: "This test case verifies that your system's LDAP server can host and return the expected address-bound X.509 certificate.",
RTM_Sections: "2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22",
RFC_2798:  "Section 9.1.2",
Instructions: "Enter a Direct address corresponding to an address-bound X.509 certificate that is hosted by your system's LDAP server and then click Submit. DCDT will attempt to discover the certificate and display the result on the screen."
},
{ code: "H4_LDAP_DB_Normal", name: "H4 - Normal domain-bound certificate search in LDAP",
Binding_Type: "DOMAIN",
Location_Type: "LDAP",
Negative: "false",
Optional: "false",
Description: "This test case verifies that your system's LDAP server can host and return the expected domain-bound X.509 certificate.",
RTM_Sections: "2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 20, 21, 22",
RFC_2798:  "Section 9.1.2",
Instructions: "Enter a Direct address corresponding to a domain-bound X.509 certificate that is hosted by your system's LDAP server and then click Submit. DCDT will attempt to discover the certificate and display the result on the screen."
}];


$scope.selectedItem = $scope.processes[0];
$scope.discorySelectedItem = $scope.discoveryTestCase[0];

   $scope.onSelectionChange= function(selectedItem,testcase) {

      $scope.testcase = selectedItem.code;
if ($scope.testcase !==""){
if (testcase === "process"){
$scope.alerts = [];
$scope.testCaseType = {'Hosting':true};
$scope.dcdtResult = angular.extend(selectedItem, $scope.testCaseType);
}else{
$scope.discalerts = [];
$scope.dcdtDiscoveryResult = selectedItem;
}
}else{
$scope.dcdtResult = null;
$scope.dcdtDiscoveryResult=null;
}
console.log(" selectedItem...... "+angular.toJson(selectedItem,true));

};

	$scope.closeAlert = function() {
		$scope.alerts = [];
		$timeout.cancel($scope.timeout);
	};
	$scope.closeDiscAlert = function() {
		$scope.discalerts = [];
		$timeout.cancel($scope.timeout);
	};
	function showAlert(type, msg) {
		$scope.alerts = [];
		$scope.alerts.push({
			type: type,
			msg: msg
		});
		$scope.timeout = $timeout($scope.closeAlert, 60000);
	}
	function showDiscAlert(type, msg) {
		$scope.discalerts = [];
		$scope.discalerts.push({
			type: type,
			msg: msg
		});
		$scope.timeout = $timeout($scope.closeDiscAlert, 60000);
	}

        $scope.restdata = function() {
console.log("hostingProcess......" );
             $scope.dcdtResult = null;
             $scope.selectedItem = $scope.processes[0];
             $scope.directAddress ="";
             $scope.testcase =$scope.selectedItem.code;
             $scope.alerts = [];
        };
$scope.resetDiscData = function() {
console.log("hostingProcess......" );
 $scope.discResultEmailAddr = "";
 $scope.discEmailAddr = "";
$scope.discoveryReport  =[];
};
$scope.discValidate = function() {
    console.log(" $scope.discEmailAddr ::::"+ angular.toJson($scope.discEmailAddr,true));
    console.log(" $scope.discResultEmailAddr ::::"+ angular.toJson($scope.discResultEmailAddr,true));
if (!$scope.discEmailAddr || $scope.discEmailAddr === "") {
showDiscAlert('danger', 'Direct Address must be an email');
}else if (!$scope.discResultEmailAddr || $scope.discResultEmailAddr === "") {
showDiscAlert('danger', 'Result Address must be an email');
}else{
$scope.discalerts = [];
   $scope.discValidateRequest = {
           "@type": "discoveryTestcaseMailMapping",
           "directAddr": $scope.discEmailAddr,
           "resultsAddr": $scope.discResultEmailAddr,
           "year": $scope.pageTitle,
           "hostingcase":"NO"
           };
           DCDTValidatorFactory.save($scope.discValidateRequest, function(data) {
              console.log(" $scope.response dcdt::::"+ angular.toJson(data,true));
             // $scope.hostingResults = angular.extend($scope.hostingResults, data);
              $scope.discoveryReport = data;
            }, function(data) {
               $scope.laddaLoading = false;
               throw {
                   code: data.data.code,
                   url: data.data.url,
                   message: data.data.message
               };
           });
}
};
        $scope.validate = function() {
             console.log(" $scope.directAddress ::::"+ angular.toJson($scope.directAddress,true));
             console.log(" $scope.testcase ::::"+ angular.toJson($scope.testcase,true));
      if (!$scope.directAddress || $scope.directAddress === "") {
      showAlert('danger', 'Direct Address must be an email');
      }else if ($scope.testcase === "") {
      showAlert('danger', 'Please Select a Hosting Testcase');
      }else{
$scope.alerts = [];
            $scope.validator = {
                    "@type": "hostingTestcaseSubmission",
                    "directAddr": $scope.directAddress,
                    "testcase": $scope.testcase,
                    "year": $scope.pageTitle,
                    "hostingcase":"YES"
                   };
                    DCDTValidatorFactory.save($scope.validator, function(data) {
                       console.log(" $scope.response dcdt::::"+ angular.toJson(data,true));
                      // $scope.hostingResults = angular.extend($scope.hostingResults, data);
                       $scope.hostingReport = data;
                     }, function(data) {
                        $scope.laddaLoading = false;
                        throw {
                            code: data.data.code,
                            url: data.data.url,
                            message: data.data.message
                        };
                    });
      }
        };



     $scope.apiUrl = ApiUrl.get();

    }
]);

dcdtValidator.filter('isEmpty', [function() {
return function(object) {
console.log("object empty...."+angular.equals({}, object));
return (angular.equals({}, object) || angular.equals([], object));
};
}]);
