
<?php if(auth()->guard()->guest()): ?>
<?php else: ?>

  <div class="sidebar">
    <img src="https://triphase.com/img/support/46/config.png" class="img-fluid">
    <a class="active" href="/metarules">Home</a>
    <a href="/metarules">Meta-Rules</a>
    <a href="/openhab-local">OpenHAB Local</a>
    <a href="/openhab-cloud">OpenHAB Cloud</a>
    <a href="/results">Results</a>
    <a href="<?php echo e(route('logout')); ?>" onclick="event.preventDefault(); document.getElementById('logout-form').submit();">
       <?php echo e(__('Logout')); ?>

    </a>
  </div>
<?php endif; ?>

<!--- stylsheet --->
<style>
body {
  margin: 0;
  font-family: "Lato", sans-serif;
}

.sidebar {
  margin: 0;
  padding: 0;
  width: 200px;
  background-color: #f1f1f1;
  position: fixed;
  height: 100%;
  overflow: auto;
}

.sidebar a {
  display: block;
  color: black;
  padding: 16px;
  text-decoration: none;
}

.sidebar a.active {
  background-color: #3490dc;
  color: white;
}

.sidebar a:hover:not(.active) {
  background-color: #555;
  color: white;
}

div.content {
  margin-left: 200px;
  padding: 1px 16px;
  height: 1000px;
}

@media  screen and (max-width: 700px) {
  .sidebar {
    width: 100%;
    height: auto;
    position: relative;
  }
  .sidebar a {float: left;}
  div.content {margin-left: 0;}
}

@media  screen and (max-width: 400px) {
  .sidebar a {
    text-align: center;
    float: none;
  }
}
</style>
<?php /**PATH /var/www/html/imcf/resources/views/inc/sidebar.blade.php ENDPATH**/ ?>