<h2><b>Disaster Rescue Helper</b></h1>
<ul>
  <p lign="justify">
  <li><b>Description</b></li>
  : This is a project in 2018-1 "IT Concentration Education" class. This project aims to help rescue team searching the internal structure in advance by Disaster Rescue Helper. In this project, a virtual maze describing the collapsed inner building structure was created. Through the virtual maze, the robot quickly searches the situation based on Wall Following Algorithm, and at the same time, transmits direction information and outputs the internal structure map in real time to android app. In addition, the Raspberry Pie Camera transmits the internal photographs to the web consistently, allowing the rescue team to monitor the internal situation in real time. Lastly, imaginary danger zones and safe zones painted in different colors in the maze are judged as color sensors, and the map showing danger and safe zones is displayed on android app.</p>
  <li><b>System Structure</b></li>
  <li><b>Function</b></li>
  <ol>
    <li><b>Wall Following</b></li>
    [![Watch the video](https://i.imgur.com/vKb2F1B.png)](https://youtu.be/vt5fpE0bzSY)<br>
    [![Watch the video](https://img.youtube.com/vi/T-D1KVIuvjA/maxresdefault.jpg)](https://youtu.be/T-D1KVIuvjA)<br>
    [![Alt text](https://img.youtube.com/vi/WoG5qpObs9I/0.jpg)](https://www.youtube.com/watch?v=WoG5qpObs9I)<br>
    [![Alt text](https://www.youtube.com/embed/WoG5qpObs9I/0.jpg)](https://www.youtube.com/embed/WoG5qpObs9I)
    <li><b>Map Drawing</b></li>
    <li><b>Live Cam In Disaster Area</b></li>
  </ol>
  <li><b></b></li>
  <li><b>Developers</b></li>
  - Soonwon Choi (https://github.com/iamsoonwonchoi)<br>
  - Shinhyuk Park (https://github.com/snhpark)<br>
  - Hyejin Park
</ul>


<iframe width="994" height="559" src="https://www.youtube.com/embed/WoG5qpObs9I" frameborder="0" allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>


(function () {

    function addButton (group) {
        var button = group.querySelector('.toolbar-item:last-of-type').cloneNode(true);

        button.setAttribute('aria-label', "Add a Youtube video");
        button.setAttribute('data-ga-click', "");
        // https://simpleicons.org/icons/youtube.svg
        // + class="octicon"
        // + width="20"
        // + height="16"
        // -viewBox
        button.innerHTML = '<svg class="octicon" width="20" height="16" xmlns="http://www.w3.org/2000/svg" fill-rule="evenodd" clip-rule="evenodd" stroke-linejoin="round" stroke-miterlimit="1.414"><path d="M0 7.345c0-1.294.16-2.59.16-2.59s.156-1.1.636-1.587c.608-.637 1.408-.617 1.764-.684C3.84 2.36 8 2.324 8 2.324s3.362.004 5.6.166c.314.038.996.04 1.604.678.48.486.636 1.588.636 1.588S16 6.05 16 7.346v1.258c0 1.296-.16 2.59-.16 2.59s-.156 1.102-.636 1.588c-.608.638-1.29.64-1.604.678-2.238.162-5.6.166-5.6.166s-4.16-.037-5.44-.16c-.356-.067-1.156-.047-1.764-.684-.48-.487-.636-1.587-.636-1.587S0 9.9 0 8.605v-1.26zm6.348 2.73V5.58l4.323 2.255-4.32 2.24h-.002z"/></svg>';
        group.appendChild(button);

        return button;
    }

    function addMarkdown (textarea) {
        var vid,
            value = textarea.value;

        if ( textarea.selectionStart !== textarea.selectionEnd ) {
            vid = textarea.value.substring(textarea.selectionStart, textarea.selectionEnd).trim();
        } else {
            vid = window.prompt('Youtube video URL?');
        }
        vid = vid.replace(/.*v=([a-z0-9_-]+).*/gi, '$1');
        textarea.value = `${value.substring(0, textarea.selectionStart)}
[![](https://img.youtube.com/vi/${vid}/0.jpg)](http://www.youtube.com/watch?v=${vid} "Click to play on Youtube.com")
${value.substring(textarea.selectionEnd)}`;
    }

    function enhanceToolbar(commentForm) {
        var textarea = commentForm.querySelector('.comment-form-textarea'),
            toolbarGroup = commentForm.querySelector('.toolbar-group:last-of-type'),
            button;

        if ( !textarea || !toolbarGroup ) {
            return;
        }

        button = addButton(toolbarGroup);
        button.onclick = function (e) {
            e.stopPropagation();
            addMarkdown(textarea);
        };
    }

    Array.prototype.forEach.call(document.querySelectorAll('.previewable-comment-form'), enhanceToolbar);
})();
