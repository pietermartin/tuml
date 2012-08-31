<html>
<head>	
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />  
	<!-- /restAndJson must be come a template variable, resolved by restlet to the app name -->
	<link type="text/css" rel="stylesheet" href="/${app.rootUrl}/javascript/layout/layout-default-latest.css" />
	<link rel="stylesheet" href="/${app.rootUrl}/javascript/slickgrid/slick.grid.css" type="text/css"/>
	<link rel="stylesheet" href="/${app.rootUrl}/javascript/slickgrid/css/smoothness/jquery-ui-1.8.16.custom.css" type="text/css"/>
	<link rel="stylesheet" href="/${app.rootUrl}/javascript/slickgrid/controls/slick.pager.css" type="text/css"/>
	<link rel="stylesheet" href="/${app.rootUrl}/javascript/slickgrid/examples/examples.css" type="text/css"/>
	<link rel="stylesheet" href="/${app.rootUrl}/javascript/slickgrid/controls/slick.columnpicker.css" type="text/css"/>
	<link rel="stylesheet" href="/${app.rootUrl}/javascript/tuml/tuml.css" type="text/css"/>

	<style>
		.slick-headerrow-column {
		      background: #87ceeb;
		      text-overflow: clip;
		      -moz-box-sizing: border-box;
		      box-sizing: border-box;
	    }

	    .slick-headerrow-column input {
	      margin: 0;
	      padding: 0;
	      width: 100%;
	      height: 100%;
	      -moz-box-sizing: border-box;
	      box-sizing: border-box;
	    }


		.cell-title {
			font-weight: bold;
		}
		.cell-effort-driven {
			text-align: center;
		}
		.cell-selection {
			border-right-color: silver;
			border-right-style: solid;
			background: #f5f5f5;
			color: gray;
			text-align: right;
			font-size: 10px;
		}
		.slick-row.selected .cell-selection {
			background-color: transparent; /* show default selected row background */
		}
	</style>

	<script src="/${app.rootUrl}/javascript/tuml/tuml.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/lib/jquery-1.7.min.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/lib/jquery-ui-1.8.16.custom.min.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/lib/jquery.event.drag-2.0.min.js"></script>
	<script src="/${app.rootUrl}/javascript/layout/jquery.layout-latest.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/slick.core.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/slick.grid.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/slick.dataview.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/slick.formatters.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/tuml.slick.formatters.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/controls/slick.pager.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/controls/slick.columnpicker.js"></script>

	<script src="/${app.rootUrl}/javascript/slickgrid/plugins/slick.cellrangedecorator.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/plugins/slick.cellrangeselector.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/plugins/slick.cellselectionmodel.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/plugins/slick.rowselectionmodel.js"></script>
	<script src="/${app.rootUrl}/javascript/slickgrid/slick.editors.js"></script>


	<script>
	var grid;
	var myLayout;
	$(document).ready(function() {
		// $('body').layout({ applyDefaultStyles: true, resizable: true });
		// init instance var
		var myLayout = $('body').layout({

			//	reference only - these options are NOT required because 'true' is the default
			closable: true // pane can open & close
			,
			resizable: true // when open, pane can be resized 
			,
			slidable: true // when closed, pane can 'slide' open over other panes - closes on mouse-out
			,
			livePaneResizing: true

			//	some resizing/toggling settings
			,
			north__slidable: false // OVERRIDE the pane-default of 'slidable=true'
			,
			north__togglerLength_closed: '100%' // toggle-button is full-width of resizer-bar
			,
			north__spacing_closed: 20 // big resizer-bar when open (zero height)
			,
			south__resizable: false // OVERRIDE the pane-default of 'resizable=true'
			,
			south__spacing_open: 0 // no resizer-bar when open (zero height)
			,
			south__spacing_closed: 20 // big resizer-bar when open (zero height)
			//	some pane-size settings
			,
			west__minSize: 100,
			east__size: 300,
			east__minSize: 200,
			east__maxSize: .5 // 50% of layout width
			,
			center__minWidth: 100

			//	some pane animation settings
			,
			west__animatePaneSizing: false,
			west__fxSpeed_size: "fast" // 'fast' animation when resizing west-pane
			,
			west__fxSpeed_open: 1000 // 1-second animation when opening west-pane
			,
			west__fxSettings_open: {
				easing: "easeOutBounce"
			} // 'bounce' effect when opening
			,
			west__fxName_close: "none" // NO animation when closing west-pane
			//	enable showOverflow on west-pane so CSS popups will overlap north pane
			,
			west__showOverflowOnHover: true

			//	enable state management
			,
			stateManagement__enabled: true // automatic cookie load & save enabled by default
			,
			showDebugMessages: true // log and/or display messages from debugging & testing code
		});

		// layout utilities
		// myLayout.addPinBtn("#myPinButton", "west");
		myLayout.allowOverflow("north");

		refreshPageTo("${app.uri}");

	});

window.onpopstate = function(event) {
	var pathname = document.location.pathname.replace("/ui", "");
	refreshPageTo(pathname);
};

</script>
</head>
<body>
	<div class="ui-layout-center">

<!-- 		<div class="grid-header" style="width:100%">
			<label>Replace with label</label>
		</div>
		<div id="myGrid" style="width:100%;height:90%;"></div>
		<div id="pager" style="width:100%;height:20px;"></div>
 -->		
	</div>

	</div>
	<div class="ui-layout-north">North</div>
	<div class="ui-layout-south">South</div>
	<div class="ui-layout-east">East</div>
	<div class="ui-layout-west">
		<ul class="ui-left-menu-link">
		</ul>
	</div>
</body>
</html>