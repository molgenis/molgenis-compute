var labelType, useGradients, nativeTextSupport, animate, json, fd, fd5;

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

function init(){
    // init data

    $.ajax({
        type : 'GET',
        dataType : 'json',
        url : '/plugin/extra/test',
        //data : JSON.stringify(data),
        contentType : 'application/json',
        success : function(data)
        {
            console.log(data);
            process(data);
        },
        error : function(data)
        {
        }

        });
}

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
            tree(data);
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

function change()
{
      fd5 = new $jit.ForceDirected({
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
            color: '#070707',
            lineWidth: 1.0
        },
        //Native canvas text styling
        Label: {
            type: 'HTML',
            size: 10,
            style: 'bold'
        },
        //Add Tips
        Tips: {
            enable: false,
            type: 'HTML',
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
            type: 'HTML',
            //Change cursor style when hovering a node
            onMouseEnter: function() {
                console.log('enter');
                fd5.canvas.getElement().style.cursor = 'move';
            },
            onMouseLeave: function() {
                console.log('leave');
                fd5.canvas.getElement().style.cursor = '';
            },
            //Update node positions when dragged
            onDragMove: function(node, eventInfo, e) {
                var pos = eventInfo.getPos();
                node.pos.setc(pos.x, pos.y);
                fd5.plot();
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
            domElement.innerHTML = '<div style="margin-left:5px;">' + node.name + '<span> some other text inside</span></div>';
            //domElement.innerHTML = node.name;
            var style = domElement.style;
            style.fontSize = "0.8em";
            style.color = "#ddd";
            style.backgroundColor = 'darkred';
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

    fd5.loadJSON(json);
    // compute positions incrementally and animate.
    fd5.computeIncremental({
        iter: 40,
        property: 'end',
        onStep: function(perc){
            //Log.write(perc + '% loaded...');
        },
        onComplete: function(){
            //Log.write('done');
            fd5.animate({
                modes: ['linear'],
                transition: $jit.Trans.Elastic.easeOut,
                duration: 2500
            });
        }
    });
}

function tree_old()
{
    var infovis = document.getElementById('infovis');
    var w = infovis.offsetWidth, h = infovis.offsetHeight;

    //Create a new ST instance
    st = new $jit.ST({
        'injectInto': 'infovis',
        'width': w,
        'height': h,

        Node: {
            overridable:true
        },
        Edge: {
            overridable:true
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
            label.innerHTML = node.name;
            label.onclick = function() {
                st.onClick(node.id);
            };
        }
    });
    //load json data
    st.loadJSON(json);
    //compute node positions and layout
    st.compute();
    //optional: make a translation of the tree
    st.geom.translate(new $jit.Complex(-200, 0), "current");
    //Emulate a click on the root node.
    st.onClick(st.root);
}

function tree(data)
{
    var infovis = document.getElementById('infovis');
    var w = infovis.offsetWidth, h = infovis.offsetHeight;

    json = JSON.parse(data);
    console.log(json);

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
                if(node) st.enter(node);
            },
            onRightClick: function() {
                st.out();
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
     node.data.$width = 30 + Math.random() * 30;
     node.data.$height = 30 + Math.random() * 30;
     });
     */
    //compute node positions and layout
    st.compute();
    //optional: make a translation of the tree
    st.geom.translate(new $jit.Complex(0, 0), "current");
    //Emulate a click on the root node.
    //Tree.Plot.plot(st.tree, st.canvas);
    st.onClick(st.root);

    //Add input handler to switch spacetree orientation.
    var select = document.getElementById('switch');
    select.onchange = function(){
        var index = select.selectedIndex;
        var or = select.options[index].text;
        select.disabled = true;
        st.switchPosition(or, "animate", {
            onComplete: function(){
                select.disabled = false;
            }
        });
    };

    var align = document.getElementById('align');
    align.onchange = function() {
        var index = align.selectedIndex;
        var or = align.options[index].text;
        st.config.Node.align = or;
        st.refresh();
    };

    //make node list
    var elemUl = document.createElement('ul');
    $jit.Graph.Util.eachNode(st.graph, function(elem){
        var elemLi = document.createElement('li');
        elemLi.onclick = function() {
            st.select(elem.id);
        };
        elemLi.innerHTML = elem.name;
        elemUl.appendChild(elemLi);
    });
    document.getElementById('id-list').appendChild(elemUl);
}