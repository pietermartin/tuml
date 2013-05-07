(function ($) {
    // register namespace
    $.extend(true, window, {
        Tuml: {
            TumlMainViewManager: TumlMainViewManager,
            TumlFakeIndex: 0,
            GlobalOneToOneIndex: GlobalOneToOneIndex
        }
    });

    function GlobalOneToOneIndex() {

        var index = {};

        this.addToOneToOneIndex = function (fakeId, item) {
            item.marker = 'pietie' + fakeId;
            index[fakeId] = item;
        }

        this.getIndex = function (fakeId) {

            if (index[fakeId] !== undefined && index[fakeId] !== null) {
                return index[fakeId];
            }
            return null;
        }

    }

    function TumlMainViewManager(uiManager, leftMenuManager) {

        this.TumlMainViewManager = "1.0.0";
        var self = this;
        this.uiManager = uiManager;
        var oclExecuteUri;
        var instanceQueryTumlUri;
        var classQueryTumlUri;
        var contextVertexId;
        var contextChanged = true;
        this.globalOneToOneIndex = new GlobalOneToOneIndex();

        this.refresh = function (tumlUri, result) {
            var qualifiedName = result[0].meta.qualifiedName;
            var metaDataNavigatingTo = result[0].meta.to;
            var metaDataNavigatingFrom = result[0].meta.from;
            this.tumlUri = tumlUri;
            //propertyNavigatingTo is null when viewing a one
            this.propertyNavigatingTo = (metaDataNavigatingFrom == undefined ? null : findPropertyNavigatingTo(qualifiedName, metaDataNavigatingFrom));
            this.tabContainerProperty = this.propertyNavigatingTo;
            if (this.propertyNavigatingTo != null && (this.propertyNavigatingTo.oneToMany || this.propertyNavigatingTo.manyToMany)) {
                //Property is a many

                var newContextVertexId = retrieveVertexId(tumlUri);
                var savedTumlTabViewManagers = this.clearTabsOnAddOneOrMany(newContextVertexId);

                leftMenuManager.refresh(metaDataNavigatingFrom, metaDataNavigatingTo, contextVertexId);
                refreshInternal(tumlUri, result, false);

                //reorder tabs, make sure new tabs are first
                reorderTabsAfterAddOneOrMany(savedTumlTabViewManagers);
            } else {
                //Property is a one
                //If there are no data then it is for creation
                var isForCreation = result[0].data.length == 0;
                if (!isForCreation) {
                    //Only one element of the array contains data, i.e. for the return concrete type
                    for (var i = 0; i < result.length; i++) {
                        if (result[i].data.length > 0) {
                            metaDataNavigatingTo = result[i].meta.to;
                            qualifiedName = result[i].meta.qualifiedName;
                            var newContextVertexId = result[i].data[0].id;
                            var savedTumlTabViewManagers = this.clearTabsOnAddOneOrMany(newContextVertexId);

                            //If property is a one then there is n navigating from
                            leftMenuManager.refresh(metaDataNavigatingTo, metaDataNavigatingTo, contextVertexId);
                            //Do not call refreshInternal as it creates all tabs for the meta data
                            var tumlTabViewManager = this.createTabContainer(tuml.tab.Enum.Properties, result[i], tumlUri, {forLookup: false, forManyComponent: false, isOne: true, forCreation: false}, this.propertyNavigatingTo);
                            this.addToTumlTabViewManagers(tumlTabViewManager);
                            tumlTabViewManager.createTab(result[i], isForCreation);

                            //reorder tabs, make sure new tabs are first
                            reorderTabsAfterAddOneOrMany(savedTumlTabViewManagers);

                            break;
                        }
                    }
                } else {
                    //This is for creation of the one
                    qualifiedName = result[0].meta.qualifiedName;
                    var newContextVertexId = retrieveVertexId(tumlUri);
                    var savedTumlTabViewManagers = this.clearTabsOnAddOneOrMany(newContextVertexId);

                    leftMenuManager.refresh(metaDataNavigatingFrom, metaDataNavigatingTo, contextVertexId);
                    refreshInternal(tumlUri, result, true, true);

                    //reorder tabs, make sure new tabs are first
                    reorderTabsAfterAddOneOrMany(savedTumlTabViewManagers);

                }
            }

            oclExecuteUri = "/" + tumlModelName + "/" + contextVertexId + "/oclExecuteQuery";
            if (hasInstanceQuery(metaDataNavigatingTo, metaDataNavigatingFrom)) {
                instanceQueryTumlUri = "/" + tumlModelName + "/basetumlwithquerys/" + contextVertexId + "/instanceQuery";
            } else {
                instanceQueryTumlUri = '';
            }
            classQueryTumlUri = "/" + tumlModelName + "/classquery/" + contextVertexId + "/query";


            if (contextVertexId !== undefined && contextVertexId !== null && contextChanged) {
                //This is the default query tab, always open
                addDefaultQueryTab();
            }

            this.tumlTabViewManagers[0].open = true;
            this.tabContainer.tabs("option", "active", 0);

            this.updateNavigationHeader(qualifiedName);
//            addButtons();
            $('body').layout().resizeAll();
        }

        this.doSave = function (commit) {
            var startTime = new Date().getTime();
            var tumlTabViewManagers = this.getTumlTabManyOrOneViewManagers(commit);
            var overloadedPostData = {insert: [], update: [], delete: []};

            if (commit && !validateMultiplicity(tumlTabViewManagers)) {
                return;
            } else {
                for (var i = 0; i < tumlTabViewManagers.length; i++) {
                    var tumlTabViewManager = tumlTabViewManagers[i];
                    if (tumlTabViewManager instanceof Tuml.TumlTabManyViewManager && !tumlTabViewManager.oneManyOrQuery.forManyComponent) {
                        var dataView = tumlTabViewManager.tumlTabGridManager.dataView;
                        overloadedPostData.insert.push.apply(overloadedPostData.insert, dataView.getNewItems());
                        overloadedPostData.update.push.apply(overloadedPostData.update, dataView.getUpdatedItems());
                        overloadedPostData.delete.push.apply(overloadedPostData.delete, dataView.getDeletedItems());
                    } else {
                        if (tumlTabViewManager.oneManyOrQuery.forCreation) {
                            overloadedPostData.insert.push(tumlTabViewManager.tumlTabOneManager.fieldsToObject());
                        } else {
                            overloadedPostData.update.push(tumlTabViewManager.tumlTabOneManager.fieldsToObject());
                        }
                        break;
                    }
                }
                var postUri;
                if (!commit) {
                    postUri = self.tumlUri + "?rollback=true";
                } else {
                    postUri = self.tumlUri;
                }

                $.ajax({
                    url: postUri,
                    type: "POST",
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify(overloadedPostData),
                    success: function (result, textStatus, jqXHR) {
                        if (commit) {
                            self.updateTabsForResultAfterCommit(result);
                        } else {
                            var endTimeBeforeUpdateGrids = new Date().getTime();
                            console.log("Time taken in millis for server call before update grids = " + (endTimeBeforeUpdateGrids - startTime));
                            self.updateTabsForResultAfterRollback(result);
                            endTimeBeforeUpdateGrids = new Date().getTime();
                            console.log("Time taken in millis for server call after  update grids = " + (endTimeBeforeUpdateGrids - startTime));
                        }
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        $('#serverErrorMsg').addClass('server-error-msg').html(jqXHR.responseText);
                    }
                });
            }
        }

        this.updateTabsForResultAfterCommit = function (result) {
            for (var i = 0; i < result.length; i++) {
                var resultForTab = result[i];
                for (var j = 0; j < this.tumlTabViewManagers.length; j++) {
                    var tumlTabViewManager = this.tumlTabViewManagers[j];
                    if (tumlTabViewManager instanceof Tuml.TumlTabManyViewManager) {
                        tumlTabViewManager.beginUpdate();
                        var metaForData = resultForTab.meta.to;
                        //TOTO use qualified name somehow
                        if (tumlTabViewManager.tabId == metaForData.name) {
                            for (var k = 0; k < resultForTab.data.length; k++) {
                                tumlTabViewManager.updateGridAfterCommitOrRollback(resultForTab.data[k]);
                            }
                        }
                        tumlTabViewManager.endUpdate(false);
                    }
                }
            }
            for (var j = 0; j < this.tumlTabViewManagers.length; j++) {
                var tumlTabViewManager = this.tumlTabViewManagers[j];
                if (tumlTabViewManager instanceof Tuml.TumlTabManyViewManager) {
                    tumlTabViewManager.clearArraysAfterCommit();
                }
            }
        }

        this.updateTabsForResultAfterRollback = function (result) {
            for (var i = 0; i < result.length; i++) {
                var resultForTab = result[i];
                for (var j = 0; j < this.tumlTabViewManagers.length; j++) {
                    var tumlTabViewManager = this.tumlTabViewManagers[j];
                    if (tumlTabViewManager instanceof Tuml.TumlTabManyViewManager) {
                        tumlTabViewManager.beginUpdate();
                        var metaForData = resultForTab.meta.to;
                        //TODO use qualified name somehow
                        //Line up he result with the correct tab
                        if (tumlTabViewManager.tabId == metaForData.name) {
                            for (var k = 0; k < resultForTab.data.length; k++) {
                                //Need to update the id's to the tmpId as the id no longer exist on a rolled back transaction
                                //Go through all the properties, for each composite property set the id = tmpId
                                this.setComponentIdToTmpId(resultForTab.data[k]);
                                tumlTabViewManager.updateGridAfterCommitOrRollback(resultForTab.data[k]);
                            }
                        }
                        tumlTabViewManager.endUpdate(true);
                    }
                }
            }
        }

        this.clearTabsOnAddOneOrMany = function (newContextVertexId) {
            if (newContextVertexId === undefined && contextVertexId === null) {
                contextChanged = true;
            } else if (contextVertexId === undefined && newContextVertexId === null) {
                contextChanged = true;
            } else if (contextVertexId === undefined && newContextVertexId === undefined) {
                contextChanged = true;
            } else {
                contextChanged = newContextVertexId !== contextVertexId;
            }
            contextVertexId = newContextVertexId;
            if (contextChanged) {
                this.destroyTabContainer();
                this.maybeCreateTabContainer();
            } else {
                this.removeValidationWarningHeader();
                var tumlTabViewManagersToClose = [];
                //Remove property tabs only, query tabs remain for the context
                for (var i = 0; i < this.tumlTabViewManagers.length; i++) {
                    var tumlTabViewManager = this.tumlTabViewManagers[i];
                    if (tumlTabViewManager instanceof  Tuml.TumlTabOneViewManager || tumlTabViewManager instanceof  Tuml.TumlTabManyViewManager) {
                        tumlTabViewManagersToClose.push(tumlTabViewManager);
                    }
                }
                for (var i = 0; i < tumlTabViewManagersToClose.length; i++) {
                    tumlTabViewManagersToClose[i].closeTab();
                }
            }
            //Save current tabs to help with reordering 2 lines down
            var savedTumlTabViewManagers = [];
            for (var i = 0; i < this.tumlTabViewManagers.length; i++) {
                var tumlTabViewManager = this.tumlTabViewManagers[i];
                savedTumlTabViewManagers.push(tumlTabViewManager);
            }
            return savedTumlTabViewManagers;
        }

        this.addQueryTab = function (post, query) {
            //Check is there is already a tab open for this query
            var tumlTabViewManagerQuery;
            var tabIndex = 0;
            for (var j = 0; j < this.tumlTabViewManagers.length; j++) {
                if (this.tumlTabViewManagers[j].tabDivName == query.getDivName()) {
                    tumlTabViewManagerQuery = this.tumlTabViewManagers[j];
                    tabIndex = j;
                    break;
                }
            }
            if (tumlTabViewManagerQuery === undefined) {
                if (query.queryType === undefined) {
                    tumlTabViewManagerQuery = new Tuml.TumlTabQueryViewManager(tuml.tab.Enum.Properties, this.tabContainer, instanceQueryTumlUri, classQueryTumlUri, query.getDivName(), query.name, query.id);
                } else if (query.queryType === 'instanceQuery') {
                    tumlTabViewManagerQuery = new Tuml.TumlTabQueryViewManager(tuml.tab.Enum.InstanceQueries, this.tabContainer, instanceQueryTumlUri, '', query.getDivName(), query.name, query.id);
                } else {
                    tumlTabViewManagerQuery = new Tuml.TumlTabQueryViewManager(tuml.tab.Enum.ClassQueries, this.tabContainer, '', classQueryTumlUri, query.getDivName(), query.name, query.id);
                }
                tumlTabViewManagerQuery.createTab();
                tumlTabViewManagerQuery.parentTabContainerManager = this;

                if (query.id === -1) {
                    this.tumlTabViewManagers.push(tumlTabViewManagerQuery);
                    reorderTabs();
                    this.tabContainer.tabs("option", "active", this.tumlTabViewManagers.length - 1);
                } else {
                    this.tumlTabViewManagers.splice(this.tumlTabViewManagers.length - 1, 0, tumlTabViewManagerQuery);
                    reorderTabs();
                    this.tabContainer.tabs("option", "active", this.tumlTabViewManagers.length - 2);
                }

                tumlTabViewManagerQuery.createQuery(oclExecuteUri, query, post);
                tumlTabViewManagerQuery.onPutInstanceQuerySuccess.subscribe(function (e, args) {
                    tumlTabViewManagerQuery.closeTab();
                    var newTumlTabViewManager = this.addQueryTab(false, new Tuml.Query(args.query.id, args.query.name, args.query.name, args.query.queryString, args.query.queryEnum, args.gridData, args.queryType));
                    leftMenuManager.refreshInstanceQuery(args.query.id);
                });
                tumlTabViewManagerQuery.onPostInstanceQuerySuccess.subscribe(function (e, args) {
                    var previousIndex = self.tumlTabViewManagers.indexOf(tumlTabViewManagerQuery);
                    tumlTabViewManagerQuery.closeTab();
                    addDefaultQueryTab(false);
                    var newTumlTabViewManager = this.addQueryTab(false, new Tuml.Query(args.query.id, args.query.name, args.query.name, args.query.queryString, args.query.queryEnum, args.gridData, args.queryType));

                    //place it back at the previousIndex
                    var currentIndex = self.tumlTabViewManagers.indexOf(newTumlTabViewManager);
                    this.tumlTabViewManagers.splice(currentIndex, 1);
                    this.tumlTabViewManagers.splice(previousIndex, 0, newTumlTabViewManager);
                    this.tabContainer.tabs("option", "active", previousIndex);

                    leftMenuManager.refreshInstanceQuery(args.query.id);
                });
                tumlTabViewManagerQuery.onPutClassQuerySuccess.subscribe(function (e, args) {
                    tumlTabViewManagerQuery.closeTab();
                    var newTumlTabViewManager = this.addQueryTab(false, new Tuml.Query(args.query.id, args.query.name, args.query.name, args.query.queryString, args.query.queryEnum, args.gridData, args.queryType));
                    leftMenuManager.refreshClassQuery(args.query.id);
                });
                tumlTabViewManagerQuery.onPostClassQuerySuccess.subscribe(function (e, args) {
                    var previousIndex = self.tumlTabViewManagers.indexOf(tumlTabViewManagerQuery);
                    closeTab(tumlTabViewManagerQuery);
                    addDefaultQueryTab(false);
                    var newTumlTabViewManager = addQueryTab(false, new Tuml.Query(args.query.id, args.query.name, args.query.name, args.query.queryString, args.query.queryEnum, args.gridData, args.queryType));

                    //place it back at the previousIndex
                    var currentIndex = self.tumlTabViewManagers.indexOf(newTumlTabViewManager);
                    self.tumlTabViewManagers.splice(currentIndex, 1);
                    self.tumlTabViewManagers.splice(previousIndex, 0, newTumlTabViewManager);
                    tabContainer.tabs("option", "active", previousIndex);

                    leftMenuManager.refreshClassQuery(args.query.id);
                });
                tumlTabViewManagerQuery.onDeleteQuerySuccess.subscribe(function (e, args) {
                    closeTab(tumlTabViewManagerQuery);
                    leftMenuManager.refreshQuery();
                });
                tumlTabViewManagerQuery.onSelfCellClick.subscribe(function (e, args) {
                    self.onSelfCellClick.notify(args, e, self);
                });

            } else {
                //Just make the tab active
                tabContainer.tabs("option", "active", self.tumlTabViewManagers.indexOf(tumlTabViewManagerQuery));
            }
            return tumlTabViewManagerQuery;

        }

        this.onPutSuccess = new Tuml.Event();
        this.onPutFailure = new Tuml.Event();
        this.onPostSuccess = new Tuml.Event();
        this.onPostFailure = new Tuml.Event();
        this.onDeleteSuccess = new Tuml.Event();
        this.onDeleteFailure = new Tuml.Event();
        this.onCancel = new Tuml.Event();
        this.onSelfCellClick = new Tuml.Event();
        this.onContextMenuClickDelete = new Tuml.Event();

        this.onPutOneSuccess = new Tuml.Event();
        this.onPostOneSuccess = new Tuml.Event();
        this.onDeleteOneSuccess = new Tuml.Event();
        this.onPutOneFailure = new Tuml.Event();
        this.onPostOneFailure = new Tuml.Event();
        this.onPostInstanceQuerySuccess = new Tuml.Event();
        this.onPutInstanceQuerySuccess = new Tuml.Event();
        this.onPostClassQuerySuccess = new Tuml.Event();
        this.onPutClassQuerySuccess = new Tuml.Event();

        function reorderTabsAfterAddOneOrMany(savedTumlTabViewManagers) {
            for (var i = 0; i < savedTumlTabViewManagers.length; i++) {
                var tumlTabViewManager = savedTumlTabViewManagers[i];
                var index = self.tumlTabViewManagers.indexOf(tumlTabViewManager)
                self.tumlTabViewManagers.splice(index, 1);
            }
            for (var i = 0; i < savedTumlTabViewManagers.length; i++) {
                self.tumlTabViewManagers.push(savedTumlTabViewManagers[i]);
            }
            reorderTabs();
        }

        function hasInstanceQuery(metaDataNavigatingTo, metaDataNavigatingFrom) {
            var properties;
            if (metaDataNavigatingFrom !== undefined) {
                properties = metaDataNavigatingFrom.properties;
            } else {
                properties = metaDataNavigatingTo.properties;
            }
            for (var i = 0; i < properties.length; i++) {
                var property = properties[i];
                if (property.name !== 'instanceQuery') {
                    return true;
                }
            }
            return false;
        }

        function addDefaultQueryTab() {
            self.addQueryTab(true, new Tuml.Query(-1, 'New Query', 'New Query Description', 'self.name', 'ocl'));
        }

        function validateMultiplicity(tumlTabManyViewManagers) {
            var rowCount = 0;
            for (var i = 0; i < tumlTabManyViewManagers.length; i++) {
                var tumlTabManyViewManager = tumlTabManyViewManagers[i];
                //Need to travel down components and validate their multiplicities
                if (tumlTabManyViewManager instanceof Tuml.TumlTabManyViewManager && !tumlTabManyViewManager.oneManyOrQuery.forManyComponent) {

                    var dataView = tumlTabManyViewManager.tumlTabGridManager.dataView;

                    rowCount += dataView.getItems().length;
                } else {
                    return true;
                }
            }
            if (rowCount < self.propertyNavigatingTo.lower || (self.propertyNavigatingTo.upper !== -1 && rowCount > self.propertyNavigatingTo.upper)) {
                alert('multiplicity falls outside the valid range [' + self.propertyNavigatingTo.lower + '..' + self.propertyNavigatingTo.upper + ']');
                return false;
            } else {
                return true;
            }
        }

        function refreshInternal(tumlUri, result, isOne, forCreation) {
            //A tab is created for every element in the array,
            //i.e. for every concrete subset of the many property
            for (var i = 0; i < result.length; i++) {
                var tumlTabViewManager = self.createTabContainer(tuml.tab.Enum.Properties, result[i], tumlUri, {forLookup: false, forManyComponent: false, isOne: isOne, forCreation: forCreation}, self.propertyNavigatingTo);
                self.addToTumlTabViewManagers(tumlTabViewManager);
                tumlTabViewManager.createTab(result[i], forCreation);
            }
        }

        function reorderTabs() {
            var tabsNav = self.tabContainer.find(".ui-tabs-nav");
            var first = true;
            for (var j = 0; j < self.tumlTabViewManagers.length; j++) {
                var li = $('#li' + self.tumlTabViewManagers[j].tabDivName);
                if (first) {
                    tabsNav.append(li);
                } else {
                    li.insertAfter(tabsNav);
                }
                tabsNav = li;
                first = false;
            }
            self.tabContainer.tabs("refresh");
        }

        function findPropertyNavigatingTo(qualifiedName, metaDataNavigatingFrom) {
            if (metaDataNavigatingFrom == undefined) {
                return null;
            } else {
                //The property one is navigating from is in the metaDataNavigatingFrom,
                //Find the property with the qualifiedName for the metaDataNavigatingTo.qualifiedName
                for (var i = 0; i < metaDataNavigatingFrom.properties.length; i++) {
                    var property = metaDataNavigatingFrom.properties[i];
                    if (property.qualifiedName == qualifiedName) {
                        return property;
                    }
                }
                alert('Property navigatingTo not found!!!');
                return null;
            }
        }

        Tuml.TumlTabContainerManager.call(this, this.tabContainer);

    }

    TumlMainViewManager.prototype = new Tuml.TumlTabContainerManager;

    TumlMainViewManager.prototype.refreshContext = function (tumlUri) {
        this.uiManager.refresh(tumlUri);
    }

    TumlMainViewManager.prototype.addToOneToOneIndex = function (fakeId, item) {
        this.globalOneToOneIndex.addToOneToOneIndex(fakeId, item);
    }

    TumlMainViewManager.prototype.updateDataModel = function (fakeId, fieldName, one) {
        var indexForFakeId = this.globalOneToOneIndex.getIndex(fakeId);
        if (indexForFakeId !== null) {
            for (var j = 0; j < this.tumlTabViewManagers.length; j++) {
                var tumlTabViewManager = this.tumlTabViewManagers[j];
                tumlTabViewManager.updateOne(fakeId, fieldName, one, indexForFakeId);
            }
        }
    }

    TumlMainViewManager.prototype.doCancel = function () {
        this.enableButtons();
        var self = this;
        Slick.GlobalEditorLock.cancelCurrentEdit();
        $.ajax({
            url: this.tumlUri,
            type: "GET",
            dataType: "json",
            contentType: "application/json",
            success: function (result, textStatus, jqXHR) {
                self.refresh(self.tumlUri, result);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $('#serverErrorMsg').addClass('server-error-msg').html(jqXHR.responseText);
            }
        });
    }

    TumlMainViewManager.prototype.saveTabs = function () {
        this.doSave(true);
    }

    TumlMainViewManager.prototype.getTabId = function () {
        return 'tabs-layout';
    }

    TumlMainViewManager.prototype.addNewRow = function (event) {
        this.doSave(false);
    }

    TumlMainViewManager.prototype.handleLookup = function (lookupUri, qualifiedName, loadDataCallback) {
        var startTime = new Date().getTime();
        var tumlTabViewManagers = this.getTumlTabManyOrOneViewManagers(false);
        var overloadedPostData = {insert: [], update: [], delete: []};

        var AJAX_TYPE;

        for (var i = 0; i < tumlTabViewManagers.length; i++) {
            var tumlTabViewManager = tumlTabViewManagers[i];
            if (tumlTabViewManager instanceof Tuml.TumlTabManyViewManager && !tumlTabViewManager.oneManyOrQuery.forManyComponent) {
                AJAX_TYPE = "POST";
                if (!tumlTabViewManager.oneManyOrQuery.forManyComponent) {
                    var dataView = tumlTabViewManager.tumlTabGridManager.dataView;
                    overloadedPostData.insert.push.apply(overloadedPostData.insert, dataView.getNewItems());
                    overloadedPostData.update.push.apply(overloadedPostData.update, dataView.getUpdatedItems());
                    overloadedPostData.delete.push.apply(overloadedPostData.delete, dataView.getDeletedItems());
                }
            } else {
                AJAX_TYPE = "PUT";
                overloadedPostData = tumlTabViewManager.tumlTabOneManager.fieldsToObject();
                break;
            }
        }
        var postUri;
        if (AJAX_TYPE === "POST") {
            postUri = this.tumlUri + "_forwardToLookup?lookupUri=" + lookupUri + '&qualifiedName=' + qualifiedName;
        } else {
            postUri = this.tumlUri + "/forwardToLookup?lookupUri=" + lookupUri + '&qualifiedName=' + qualifiedName;
        }

        $.ajax({
            url: postUri,
            type: AJAX_TYPE,
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify(overloadedPostData),
            success: function (result, textStatus, jqXHR) {
                var endTimeBeforeUpdateGrids = new Date().getTime();
                console.log("Time taken in millis for server call before update drop down = " + (endTimeBeforeUpdateGrids - startTime));

                //Make sure id's are replaced with tmpId where needed
                for (var i = 0; i < result.data.length; i++) {
                    var item = result.data[i];
                    if (item.tmpId !== undefined && item.tmpId !== null) {
                        item.id = item.tmpId;
                    }
                }
                loadDataCallback(result.data);
                endTimeBeforeUpdateGrids = new Date().getTime();
                console.log("Time taken in millis for server call after  update drop down = " + (endTimeBeforeUpdateGrids - startTime));
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $('#serverErrorMsg').addClass('server-error-msg').html(jqXHR.responseText);
            }
        });
    }

})
    (jQuery);
