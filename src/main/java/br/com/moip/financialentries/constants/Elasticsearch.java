package br.com.moip.financialentries.constants;

public interface Elasticsearch {

    String query = """
            {
                "size": "%d",
                "query" : {
                  "bool" : {
                    "must" : [ {
                      "match" : {
                        "account" : {
                          "query" : "%s",
                          "type" : "boolean"
                        }
                      }
                    },\s
                    {
                      "match" : {
                        "status" : {
                          "query" : "SETTLED",
                          "type" : "boolean"
                        }
                      }
                    },\s
                    {
                      "range" : {
                        "settledAt" : {
                          "from" : "%s",
                          "to" : "%s",
                          "include_lower" : true,
                          "include_upper" : true
                        }
                      }
                    } ]
                  }
                },
                "sort" : [ {
                  "settledAt" : {
                    "order" : "asc"
                  }
                }, {
                  "type" : {
                    "order" : "asc"
                  }
                } ],
                "aggregations" : {
                  "previousRange" : {
                    "date_range" : {
                      "field" : "settledAt",
                      "ranges" : [ {
                        "to" : "2023-01-25 23:59:59"
                      } ],
                      "format" : "yyyy-MM-dd HH:mm:ss"
                    },
                    "aggregations" : {
                      "previousBalance" : {
                        "sum" : {
                          "field" : "liquidAmount"
                        }
                      }
                    }
                  },
                  "requestedRange" : {
                    "date_range" : {
                      "field" : "settledAt",
                      "ranges" : [ {
                        "from" : "2023-01-25 00:00:00",
                          "to" : "2023-01-25 23:59:59"
                      } ],
                      "format" : "yyyy-MM-dd HH:mm:ss"
                    },
                    "aggregations" : {
                      "creditEntries" : {
                        "range" : {
                          "field" : "liquidAmount",
                          "ranges" : [ {
                            "key" : "creditBounded",
                            "from" : 0.0
                          } ]
                        },
                        "aggregations" : {
                          "creditSum" : {
                            "sum" : {
                              "field" : "liquidAmount"
                            }
                          }
                        }
                      },
                      "debitEntries" : {
                        "range" : {
                          "field" : "liquidAmount",
                          "ranges" : [ {
                            "key" : "debitBounded",
                            "to" : 0.0
                          } ]
                        },
                        "aggregations" : {
                          "debitSum" : {
                            "sum" : {
                              "field" : "liquidAmount"
                            }
                          }
                        }
                      },
                      "dailyBalance" : {
                        "date_histogram" : {
                          "field" : "settledAt",
                          "interval" : "1d",
                          "format" : "yyyy-MM-dd"
                        },
                        "aggregations" : {
                          "amount" : {
                            "sum" : {
                              "field" : "liquidAmount"
                            }
                          },
                          "groupByType" : {
                            "terms" : {
                              "field" : "type",
                              "size" : 94
                            },
                            "aggregations" : {
                              "amount" : {
                                "sum" : {
                                  "field" : "liquidAmount"
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            """;

    String getTotalEntries = """
            {
                "size": 0,
                "query": {
                    "bool": {
                        "must": [
                            {
                                "match": {
                                    "account": {
                                        "query": "%s",
                                        "type": "boolean"
                                    }
                                }
                            }
                        ]
                    }
                }
            }
            """;

}
