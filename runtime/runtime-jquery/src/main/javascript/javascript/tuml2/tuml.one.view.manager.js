(function ($) {
    // register namespace
    $.extend(true, window, {
        Tuml:{
            TumlOneViewManager:TumlOneViewManager
        }
    });

    function TumlOneViewManager() {

        var self = this;
        var exist = false;
        var tumlTabViewManagers = [];

        function init() {
        }

        function clear() {
            tumlTabViewManagers = [];
        }

        function refresh(tumlUri, result, isForCreation, tumlOneTabViewManager) {
            self = this;
            var isForCreation = isForCreation;
            for (var i = 0; i < result.length; i++) {
                var response = result[i];
                var metaForData = response.meta.to;
                if (isForCreation || response.data.length > 0) {
                    var tumlTabViewManager = new Tuml.TumlTabViewManager({many:false, one:true, query:false, component: tumlOneTabViewManager !== undefined}, tumlUri, response);
                    if (tumlOneTabViewManager !== undefined) {
                        tumlTabViewManager.setLinkedTumlTabViewManager(tumlOneTabViewManager);
                    }
                    tumlTabViewManager.onClickOneComponent.subscribe(function (e, args) {

                        //Get the meta data
                        $.ajax({
                            url:args.property.tumlMetaDataUri,
                            type:"GET",
                            dataType:"json",
                            contentType:"json",
                            success:function (metaDataResponse, textStatus, jqXHR) {
                                $('#tab-container').tabs('disableTab', tumlTabViewManager.tabTitle);
                                tumlTabViewManager.setCell(args.property);
                                refresh(args.tumlUri, metaDataResponse, isForCreation, tumlTabViewManager);
                            },
                            error:function (jqXHR, textStatus, errorThrown) {
                                alert('error getting ' + property.tumlMetaDataUri + '\n textStatus: ' + textStatus + '\n errorThrown: ' + errorThrown)
                            }
                        });

                    });
                    tumlTabViewManager.onOneComponentAddButtonSuccess.subscribe(function (e, args) {
                        console.log('tumlTabViewManager.onOneComponentAddButtonSuccess fired')
                        tumlTabViewManager.getLinkedTumlTabViewManager().setValue(args.value);
                        tumlTabViewManager.closeTab();
                        tumlTabViewManager.getLinkedTumlTabViewManager().enableTab();
                    });
                    tumlTabViewManager.onPutOneSuccess.subscribe(function (e, args) {
                        self.onPutOneSuccess.notify(args, e, self);
                    });
                    tumlTabViewManager.onPutOneFailure.subscribe(function (e, args) {
                    });
                    tumlTabViewManager.onPostOneSuccess.subscribe(function (e, args) {
                        self.onPostOneSuccess.notify(args, e, self);
                    });
                    tumlTabViewManager.onPostOneFailure.subscribe(function (e, args) {
                    });
                    tumlTabViewManager.createTab();
                    tumlTabViewManager.createOne(response.data[0], metaForData, isForCreation);
                    tumlTabViewManagers.push(tumlTabViewManager);
                }
            }
        }

        function closeQuery(title, index) {
            tumlTabViewManagers.splice(index, 1);
        }

        function openQuery(tumlUri, oclExecuteUri, qualifiedName, tabDivName, queryEnum, queryString) {
            //Check is there is already a tab open for this query
            var tumlTabViewManagerQuery;
            var tabIndex = 0;
            for (j = 0; j < tumlTabViewManagers.length; j++) {
                if (tumlTabViewManagers[j].oneManyOrQuery.query && tumlTabViewManagers[j].tabDivName == tabDivName) {
                    tumlTabViewManagerQuery = tumlTabViewManagers[j];
                    tabIndex = j;
                    break;
                }
            }
            if (tumlTabViewManagerQuery === undefined) {
                $('#tab-container').tabs('add', {title:tabDivName, content:'<div id="' + tabDivName + '" />', closable:true});
                var tumlTabViewManager = new Tuml.TumlTabViewManager({many:false, one:false, query:true}, tumlUri, qualifiedName, tabDivName);
                tumlTabViewManagers.push(tumlTabViewManager);
                tumlTabViewManager.createQuery(oclExecuteUri, queryEnum, queryString);
            } else {
                //Just make the tab active
                $('#tab-container').tabs('select', tabIndex);
            }

        }

        function clear() {
        }

        //Public api
        $.extend(this, {
            "TumlOneViewManagerVersion":"1.0.0",
            "onPutOneSuccess":new Tuml.Event(),
            "onPutOneFailure":new Tuml.Event(),
            "onPostOneSuccess":new Tuml.Event(),
            "onPostOneFailure":new Tuml.Event(),
            "refresh":refresh,
            "openQuery":openQuery,
            "closeQuery":closeQuery,
            "clear":clear
        });

        init();
    }

})(jQuery);
