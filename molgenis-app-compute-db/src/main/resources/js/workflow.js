var labelType, useGradients, nativeTextSupport, animate, json, fd, st, st1;

(function() {
    var ua = navigator.userAgent,
        iStuff = ua.match(/iPhone/i) || ua.match(/iPad/i),
        typeOfCanvas = typeof HTMLCanvasElement,
        nativeCanvasSupport = (typeOfCanvas == 'object' || typeOfCanvas == 'function'),
        textSupport = nativeCanvasSupport
            && (typeof document.createElement('canvas').getContext('2d').fillText == 'function');
    //I'm setting this based on the fact that ExCanvas provides text support for IE
    //and that as of today iPhone/iPad current text support is lame
    labelType = (!nativeCanvasSupport || (textSupport && !iStuff))? 'Native' : 'HTML';
    nativeTextSupport = labelType == 'Native';
    useGradients = nativeCanvasSupport;
    animate = !(iStuff || !nativeCanvasSupport);
})();

/*
var Log = {
    elem: false,
    write: function(text){
        if (!this.elem)
            this.elem = document.getElementById('log');
        this.elem.innerHTML = text;
        this.elem.style.left = (500 - this.elem.offsetWidth / 2) + 'px';
    }
};
*/

function init_new()
{
    var infovis = document.getElementById('infovis');
    var w = infovis.offsetWidth, h = infovis.offsetHeight;

    $.ajax({
        type : 'GET',
        dataType : 'json',
        url : '/plugin/extra/tree',
        //data : JSON.stringify(data),
        contentType : 'application/json',
        success : function(data)
        {
            console.log(data);
            json = JSON.parse(data);
            console.log(json);
            //transform();
            tree();
        },
        error : function(data)
        {
        }

    });

}

function process(data)
{
    //json = JSON.stringify(data);
    json = JSON.parse(data);
    console.log(json);
    //change();
    //tree()
    //return;
    fd = new $jit.ForceDirected({
        //id of the visualization container
        injectInto: 'infovis',
        //Enable zooming and panning
        //by scrolling and DnD
        Navigation: {
            enable: true,
            //Enable panning events only if we're dragging the empty
            //canvas (and not a node).
            panning: 'avoid nodes',
            zooming: 10 //zoom speed. higher is more sensible
        },
        // Change node and edge styles such as
        // color and width.
        // These properties are also set per node
        // with dollar prefixed data-properties in the
        // JSON structure.
        Node: {
            overridable: true
        },
        Edge: {
            overridable: true,
            color: '#23A4FF',
            lineWidth: 0.4
        },
        //Native canvas text styling
        Label: {
            type: labelType, //Native or HTML
            size: 10,
            style: 'bold',
            color: '#070707'
        },
        //Add Tips
        Tips: {
            enable: true,
            onShow: function(tip, node) {
                //count connections
                var count = 0;
                node.eachAdjacency(function() { count++; });
                //display node info in tooltip
                tip.innerHTML = "<div class=\"tip-title\">" + node.name + "</div>"
                    + "<div class=\"tip-text\"><b>connections:</b> " + count + "</div>";
            }
        },
        // Add node events
        Events: {
            enable: true,
            type: 'Native',
            //Change cursor style when hovering a node
            onMouseEnter: function() {
                fd.canvas.getElement().style.cursor = 'move';
            },
            onMouseLeave: function() {
                fd.canvas.getElement().style.cursor = '';
            },
            //Update node positions when dragged
            onDragMove: function(node, eventInfo, e) {
                var pos = eventInfo.getPos();
                node.pos.setc(pos.x, pos.y);
                fd.plot();
            },
            //Implement the same handler for touchscreens
            onTouchMove: function(node, eventInfo, e) {
                $jit.util.event.stop(e); //stop default touchmove event
                this.onDragMove(node, eventInfo, e);
            },
            //Add also a click handler to nodes
            onClick: function(node) {
                if(!node) return;
                // Build the right column relations list.
                // This is done by traversing the clicked node connections.
                var html = "<h4>" + node.name + "</h4><b> connections:</b><ul><li>",
                    list = [];
                node.eachAdjacency(function(adj){
                    list.push(adj.nodeTo.name);
                });
                //append connections information
                $jit.id('inner-details').innerHTML = html + list.join("</li><li>") + "</li></ul>";
            }
        },
        //Number of iterations for the FD algorithm
        iterations: 200,
        //Edge length
        levelDistance: 130,
        // Add text to the labels. This method is only triggered
        // on label creation and only for DOM labels (not native canvas ones).
        onCreateLabel: function(domElement, node){
            domElement.innerHTML = node.name;
            var style = domElement.style;
            style.fontSize = "0.8em";
            style.color = "#ddd";
        },
        // Change node styles when DOM labels are placed
        // or moved.
        onPlaceLabel: function(domElement, node){
            var style = domElement.style;
            var left = parseInt(style.left);
            var top = parseInt(style.top);
            var w = domElement.offsetWidth;
            style.left = (left - w / 2) + 'px';
            style.top = (top + 10) + 'px';
            style.display = '';
        }
    });
    // load JSON data.
    fd.loadJSON(json);
    // compute positions incrementally and animate.
    //fd.plot();

    fd.computeIncremental({
        iter: 40,
        property: 'end',
        onStep: function(perc){
            //Log.write(perc + '% loaded...');
        },
        onComplete: function(){
            //Log.write('done');
            fd.animate({
                modes: ['linear'],
                transition: $jit.Trans.Elastic.easeOut,
                duration: 2500
            });
        }
    });

}

function tree()
{
    console.log("tree");

    var infovis = document.getElementById('infovis');
    var w = infovis.offsetWidth, h = infovis.offsetHeight;

//    json = JSON.parse(data);
//    console.log(json);

    //Create a new ST instance
    st = new $jit.ST({
        'injectInto': 'infovis',
        offsetY: 100,
        'width': w,
        'height': h,

        orientation: "top",
        levelDistance: 50,
        Node: {
            overridable: true,
            align: "center",
            height: 20,
            CanvasStyles: {
                strokeStyle: '#333',
                lineWidth: 2
            }
        },
        Navigation: {
            enable: true,
            //Enable panning events only if we're dragging the empty
            //canvas (and not a node).
            panning: 'avoid nodes',
            zooming: 10 //zoom speed. higher is more sensible
        },

        Edge: {
            overridable: true,
            'type': 'bezier',
            dim: 15,
            lineWidth: 2,
            color: '#333'
        },
        Label: {
            type: labelType,
            style: 'bold',
            size: 10,
            color: '#333'
        },

        Tips: {
            enable: true,
            onShow: function(tip, node) {
                //count connections
                var count = 0;
                node.eachAdjacency(function() { count++; });
                //display node info in tooltip
                tip.innerHTML = "<div class=\"tip-title\">" + node.name + "</div>"
                    + "<div class=\"tip-text\"><b>connections:</b> " + count + "</div>";
            }
        },

        Events: {
            enable: true,
            onClick: function(node) {
                if(node)
                {
                    //st.enter(node);
                    var c = node.getData('superdata');
                    $jit.id('inner-details').innerHTML = c;
                }
            },
            onRightClick: function()
            {
                //st.out();
            },
            //change node styles and canvas styles
            //when hovering a node
            onMouseEnter: function(node, eventInfo) {
                if(node) {
                    //add node selected styles and replot node
                    node.setCanvasStyle('shadowBlur', 7);
                    c1 = node.getData('color');
                    node.setData('color', '#888');
                    st.fx.plotNode(node, st.canvas);
                    st.labels.plotLabel(st.canvas, node);
                }
            },
            onMouseLeave: function(node) {
                if(node) {
                    node.setData('color', c1);
                    //node.removeData('color');
                    node.removeCanvasStyle('shadowBlur');
                    st.plot();
                }
            },
            onDragMove: function(node, eventInfo, e) {
                var pos = eventInfo.getPos();
                node.pos.setc(pos.x, pos.y);
                st.plot();
            }
        },
        onBeforeCompute: function(node){
            //Log.write("loading " + node.name);
        },

        onAfterCompute: function(node){
            //Log.write("done");
        },

        onCreateLabel: function(label, node){
            label.id = node.id;
            label.style.cursor = 'pointer';
            label.color = "ff1425";
            label.innerHTML = node.name;
            // label.onclick = function() {
//               st.onClick(node.id);
//             };
        },

        onBeforePlotNode: function(node){
            if (node.selected) {
                //node.data.$color = "#ff7";
            }
            else {
                //delete node.data.$color;
            }
        },

        onBeforePlotLine: function(adj){
            if (adj.nodeFrom.selected && adj.nodeTo.selected) {
                //adj.data.$color = "#eed";
                adj.data.$lineWidth = 3;
            }
            else {
                //delete adj.data.$color;
                delete adj.data.$lineWidth;
            }
        }
    });
    //load json data
    st.loadJSON(json);

     /*
     $jit.Graph.Util.eachNode(st.graph, function(node){
        st.addNodeInPath(node);
     });
     */

    //compute node positions and layout
    st.compute();
    //optional: make a translation of the tree
    st.geom.translate(new $jit.Complex(0, 0), "current");
    //Emulate a click on the root node.
    //Tree.Plot.plot(st.tree, st.canvas);
    st.onClick(st.root);


}

function remove_subtree()
{
    console.log("remove subtree");
    //st.removeSubtree('Start', true, 'animate', { onComplete: function() { alert('complete!'); } });
    st.graph.empty();
    st.canvas.clear();
}

function remove_subtree_clean_canvas()
{
    console.log("remote subtree and clean canvas");
    st1.removeSubtree('Start', true, 'animate', { onComplete: function() { alert('complete!'); } });
    st1.graph.empty();
    st1.canvas.clear();
}

function transform()
{
    var infovis = document.getElementById('infovis');
    var w = infovis.offsetWidth, h = infovis.offsetHeight;

    var json1 = {id: "Start",
        name: "Start",
        data: {"$color": "#23A4FF"},
        children: [
            {
                id: "o1",
                name: "op1",
                data: {"$color": "#23A4FF"},
                children: [{
                    id: "o3",
                    name: "op3",
                    data: {"$color": "#ff1425"}}]
            },
            {
                id: "o2",
                name: "op2",
                data: {"$color": "#feff43"},
                children: [{
                    id: "o3",
                    name: "op3",
                    data: {"$color": "#ff1425"}}]
            }
        ]
    };

    console.log("transform");
    var jsonpie = {
        'id': 'root',
        'name': 'RGraph based Pie Chart',
        'data': {
            '$type': 'none'
        },
        'children':[
            {
                'id':'pie1',
                'name': 'pie1',
                'data': {
                    '$angularWidth': 10,
                    '$color': '#25f'
                },
                'children': []
            },
            {
                'id':'pie2',
                'name': 'pie2',
                'data': {
                    '$angularWidth': 50,
                    '$color': '#77f'
                },
                'children': []
            },
            {
                'id':'pie3',
                'name': 'pie3',
                'data': {
                    '$angularWidth': 30,
                    '$color': '#99f'
                },
                'children': []
            },
            {
                'id':'pie4',
                'name': 'pie4',
                'data': {
                    '$angularWidth': 10,
                    '$color': '#ff1425'
                },
                'children': []
            }
        ]
    };
    //end
    var jsonpie1 = {
        'id': 'root',
        'name': 'RGraph based Pie Chart',
        'data': {
            '$type': 'none'
        },
        'children':[
            {
                'id':'pie1',
                'name': 'pie1',
                'data': {
                    '$angularWidth': 40,
                    '$color': '#77f'
                },
                'children': []
            },
            {
                'id':'pie2',
                'name': 'pie2',
                'data': {
                    '$angularWidth': 20,
                    '$color': '#45f'
                },
                'children': []
            },
            {
                'id':'pie3',
                'name': 'pie3',
                'data': {
                    '$angularWidth': 10,
                    '$color': '#99f'
                },
                'children': []
            },
            {
                'id':'pie4',
                'name': 'pie4',
                'data': {
                    '$angularWidth': 30,
                    '$color': '#ff1425'
                },
                'children': []
            }
        ]
    };

    var jsonpie2 = {
        'id': 'root',
        'name': 'RGraph based Pie Chart',
        'data': {
            '$type': 'none'
        },
        'children':[
            {
                'id':'pie1',
                'name': 'pie1',
                'data': {
                    '$angularWidth': 20,
                    '$color': '#15f'
                },
                'children': []
            },
            {
                'id':'pie2',
                'name': 'pie2',
                'data': {
                    '$angularWidth': 40,
                    '$color': '#77f'
                },
                'children': []
            },
            {
                'id':'pie3',
                'name': 'pie3',
                'data': {
                    '$angularWidth': 30,
                    '$color': '#80f'
                },
                'children': []
            },
            {
                'id':'pie4',
                'name': 'pie4',
                'data': {
                    '$angularWidth': 10,
                    '$color': '#ff1425'
                },
                'children': []
            }
        ]
    };

    var jsonpie3 = {
        'id': 'root',
        'name': 'RGraph based Pie Chart',
        'data': {
            '$type': 'none'
        },
        'children':[
            {
                'id':'pie1',
                'name': 'pie1',
                'data': {
                    '$angularWidth': 20,
                    '$color': '#35f'
                },
                'children': []
            },
            {
                'id':'pie2',
                'name': 'pie2',
                'data': {
                    '$angularWidth': 40,
                    '$color': '#77f'
                },
                'children': []
            },
            {
                'id':'pie3',
                'name': 'pie3',
                'data': {
                    '$angularWidth': 10,
                    '$color': '#50f'
                },
                'children': []
            },
            {
                'id':'pie4',
                'name': 'pie4',
                'data': {
                    '$angularWidth': 30,
                    '$color': '#ff1425'
                },
                'children': []
            }
        ]
    };

    //init nodetypes
    //Here we implement custom node rendering types for the RGraph
    //Using this feature requires some javascript and canvas experience.
    $jit.RGraph.Plot.NodeTypes.implement({
        //This node type is used for plotting pie-chart slices as nodes
        'shortnodepie': {
            'render': function(node, canvas) {
                var ldist = this.config.levelDistance;
                var span = node.angleSpan, begin = span.begin, end = span.end;
                var polarNode = node.pos.getp(true);

                var polar = new $jit.Polar(polarNode.rho, begin);
                var p1coord = polar.getc(true);

                polar.theta = end;
                var p2coord = polar.getc(true);

                polar.rho += ldist;
                var p3coord = polar.getc(true);

                polar.theta = begin;
                var p4coord = polar.getc(true);


                var ctx = canvas.getCtx();
                ctx.beginPath();
                ctx.moveTo(p1coord.x, p1coord.y);
                ctx.lineTo(p4coord.x, p4coord.y);
                ctx.moveTo(0, 0);
                ctx.arc(0, 0, polarNode.rho, begin, end, false);

                ctx.moveTo(p2coord.x, p2coord.y);
                ctx.lineTo(p3coord.x, p3coord.y);
                ctx.moveTo(0, 0);
                ctx.arc(0, 0, polarNode.rho + ldist, end, begin, true);

                ctx.fill();
            }
        }
    });

    $jit.ST.Plot.NodeTypes.implement({
        //Create a new node type that renders an entire RGraph visualization
        'piechart': {
            'render': function(node, canvas, animating) {
                var ctx = canvas.getCtx(), pos = node.pos.getc(true);
                ctx.save();
                ctx.translate(pos.x, pos.y);
                console.log("id:" + node.id);

                if(node.id == "Start")
                {
                    pie.loadJSON(jsonpie);
                    console.log("in one");
                }
                else if(node.id == "o1")
                {
                    pie.loadJSON(jsonpie1);
                    console.log("in second");
                }
                else if(node.id == "o2")
                {
                    pie.loadJSON(jsonpie2);
                    console.log("in third");
                }
                else if(node.id == "o3")
                {
                    pie.loadJSON(jsonpie3);
                }

                pie.compute();
                pie.plot();
                ctx.restore();
            }
        }
    });
    //end

    var pie = new $jit.RGraph({
        'injectInto': 'infovis',
        //Add node/edge styles and set
        //overridable=true if you want your
        //styles to be individually overriden
        Node: {
            'overridable': true,
            'type':'shortnodepie'
        },
        Edge: {
            'type':'none'
        },
        //Parent-children distance
        levelDistance: 15,
        //Don't create labels for this visualization
        withLabels: false,
        //Don't clear the canvas when plotting
        clearCanvas: false
    });

    //init st
    st1 = new $jit.ST({
        useCanvas: pie.canvas,
        orientation: 'top',
        //Add node/edge styles
        Node: {
            overridable: true,
            type: 'piechart',
            width: 60,
            height: 60
        },
        Edge: {
            overridable: true,
            color: '#333',
            type: 'quadratic:begin'
        },
        //Parent-children distance
        levelDistance: 60,
        clearCanvas: true,

        Events: {
            enable: true,
            onClick: function(node) {
                if(node) st1.enter(node);
            },
            onRightClick: function() {
                st1.out();
            },
            //change node styles and canvas styles
            //when hovering a node
            onMouseEnter: function(node, eventInfo) {
                if(node) {
                    //add node selected styles and replot node
                    node.setCanvasStyle('shadowBlur', 7);
                    c1 = node.getData('color');
                    node.setData('color', '#888');
                    st1.fx.plotNode(node, st1.canvas);
                    st1.labels.plotLabel(st1.canvas, node);
                }
            },
            onMouseLeave: function(node) {
                if(node) {
                    node.setData('color', c1);
                    //node.removeData('color');
                    node.removeCanvasStyle('shadowBlur');
                    st1.plot();
                }
            },
            onDragMove: function(node, eventInfo, e) {
                var pos = eventInfo.getPos();
                node.pos.setc(pos.x, pos.y);
                st1.plot();
            }
        },
        onBeforeCompute: function(node){
            //Log.write("loading " + node.name);
        },

        onAfterCompute: function(node){
            //Log.write("done");
        },

        onCreateLabel: function(label, node){
            label.id = node.id;
            label.style.cursor = 'pointer';
            label.color = "ff1425";
            label.innerHTML = node.name;
            // label.onclick = function() {
//               st.onClick(node.id);
//             };
        },


        //Add styles to node labels on label creation
        onCreateLabel: function(domElement, node){
            //add some styles to the node label
            var style = domElement.style;
            domElement.id = node.id;
            style.color = '#fff';
            style.fontSize = '0.8em';
            style.textAlign = 'center';
            style.width = "60px";
            style.height = "24px";
            style.paddingTop = "22px";
            style.cursor = 'pointer';
            domElement.innerHTML = node.name;
        }
    });

    //load json data
    st1.loadJSON(json1);
    //compute node positions and layout
    st1.compute();
    //optional: make a translation of the tree
    st1.geom.translate(new $jit.Complex(0, 200), "start");
    //Emulate a click on the root node.
    st1.onClick(st1.root, {
        Move: {
            offsetY: +90
        }
    });

    $jit.id('inner-details').innerHTML = st1.graph.getNode(st1.root).data.relation;
}
