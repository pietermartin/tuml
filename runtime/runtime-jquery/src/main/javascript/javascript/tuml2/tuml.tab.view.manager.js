(function ($) {
    // register namespace
    $.extend(true, window, {
        Tuml: {
            TumlTabViewManager: TumlTabViewManager
        }
    });

    function TumlTabViewManager(many, tumlUri, qualifiedName, tabDivName) {

        var self = this;
        var tumlTabGridManager;
        var tumlTabOneManager;

        function init() {
            var tabContainer = $('#tab-container');
            var tabDiv = $('<div />', {id: tabDivName}).appendTo(tabContainer);
            if (many) {
                //Do not pass the div in, it causes issues with refreshing
                tumlTabGridManager = new Tuml.TumlTabGridManager(tumlUri);
                tumlTabGridManager.onPutSuccess.subscribe(function(e, args) {
                    console.log('TumlTabViewManager onPutSuccess fired');
                    self.onPutSuccess.notify(args, e, self);
                    createGridForResult(args.data, args.tabId);
                });
                tumlTabGridManager.onPutFailure.subscribe(function(e, args) {
                    console.log('TumlTabViewManager onPutFailure fired: ' + args);
                    self.onPutFailure.notify(args, e, self);
                });
                tumlTabGridManager.onPostSuccess.subscribe(function(e, args) {
                    console.log('TumlTabViewManager onPostSuccess fired');
                    self.onPostSuccess.notify(args, e, self);
                });
                tumlTabGridManager.onPostFailure.subscribe(function(e, args) {
                    console.log('TumlTabViewManager onPostFailure fired');
                    self.onPostFailure.notify(args, e, self);
                });
                tumlTabGridManager.onDeleteSuccess.subscribe(function(e, args) {
                    console.log('TumlTabViewManager onDeleteSuccess fired');
                    self.onDeleteSuccess.notify(args, e, self);
                });
                tumlTabGridManager.onDeleteFailure.subscribe(function(e, args) {
                    console.log('TumlTabViewManager onDeleteFailure fired');
                    self.onDeleteFailure.notify(args, e, self);
                });
                tumlTabGridManager.onCancel.subscribe(function(e, args) {
                    console.log('TumlTabViewManager onCancel fired');
                    self.onCancel.notify(args, e, self);
                    createGridForResult(args.data, args.tabId);
                });
                tumlTabGridManager.onContextMenuClickLink.subscribe(function(e, args) {
                    console.log('TumlTabViewManager onContextMenuClickLink fired');
                    self.onContextMenuClickLink.notify(args, e, self);
                });
                tumlTabGridManager.onContextMenuClickDelete.subscribe(function(e, args) {
                    console.log('TumlTabViewManager onContextMenuClickDelete fired');
                    self.onContextMenuClickDelete.notify(args, e, self);
                });
            } else {
                tumlTabOneManager = new Tuml.TumlTabOneManager(tumlUri);
                tumlTabOneManager.onPutOneSuccess.subscribe(function(e, args) {
                    console.log('TumlTabViewManager onPutOneSuccess fired');
                    self.onPutOneSuccess.notify(args, e, self);
                });
                tumlTabOneManager.onPutOneFailure.subscribe(function(e, args) {
                    console.log('TumlTabViewManager onPutOneFailure fired: ' + args.textStatus + ' ' + args.errorThrown);
                    self.onPutOneFailure.notify(args, e, self);
                });
            }
        }

        function createTab(result) {
            //Add in a tab
            var tabUl = $('#tabsul');
            li = $('<li />', {class: 'tab'}).append(
                $('<a />', {href: '#' + tabDivName}).append(
                    $('<span>').text(tabDivName))).appendTo(tabUl);
        }

        function createGridForResult(result, tabId) {
            for (i = 0; i < result.length; i++) {
                if (result[i].meta.length === 3) {
                    var metaForData = result[i].meta[2];
                    if (metaForData.name === tabId) {
                        $('#' + tabDivName).children().remove();
                        createGrid(result[i]);
                        return;
                    }
                }
            }
        }

        //Grids must be created after tabs have been created, else things look pretty bad like...
        function createGrid(result) {
            tumlTabGridManager.refresh(result);
        }

        //Must be created after tabs have been created, else things look pretty bad like...
        function createOne(result, metaForData) {
            tumlTabOneManager.refresh(result, metaForData, qualifiedName);
        }

        //Public api
        $.extend(this, {
            "TumlTabViewManagerVersion": "1.0.0",
            //These events are propogated from the grid
            "onPutSuccess": new Tuml.Event(),
            "onPutFailure": new Tuml.Event(),
            "onPostSuccess": new Tuml.Event(),
            "onPostFailure": new Tuml.Event(),
            "onDeleteSuccess": new Tuml.Event(),
            "onDeleteFailure": new Tuml.Event(),
            "onCancel": new Tuml.Event(),
            "onContextMenuClickLink": new Tuml.Event(),
            "onContextMenuClickDelete": new Tuml.Event(),
            //Events for one
            "onPutOneSuccess": new Tuml.Event(),
            "onPutOneFailure": new Tuml.Event(),
            //Other events
            "createTab": createTab,
            "createOne": createOne,
            "createGrid": createGrid,
            "tabDivName": tabDivName
        });

        init();
    }

})(jQuery);
