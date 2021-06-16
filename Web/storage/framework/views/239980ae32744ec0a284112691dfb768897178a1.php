<!--- navbar-light bg-white --->
<nav class="navbar navbar-expand-md navbar-dark bg-dark shadow-sm ">
    <div class="container">
        <?php if(auth()->guard()->guest()): ?>
        <a class="navbar-brand" href="<?php echo e(url('/')); ?>">
            <?php echo e(config('app.name', 'Laravel')); ?>

        </a>
        <?php else: ?>
        <a class="navbar-brand" href="<?php echo e(url('/metarules')); ?>">
            <?php echo e(config('app.name', 'Laravel')); ?>

        </a>
        <?php endif; ?>
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="<?php echo e(__('Toggle navigation')); ?>">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <!-- Left Side Of Navbar -->
            <ul class="navbar-nav mr-auto">

              <?php if(auth()->guard()->guest()): ?>

              <?php else: ?>
              <!--  <li class="nav-item">
                  <a class="nav-link" href="/users/<?php echo e(Auth::user()->id); ?>"><?php echo e(Auth::user()->name); ?> <?php echo e(Auth::user()->surname); ?></a>
                </li> -->
              <?php endif; ?>

            </ul>


            <!-- Right Side Of Navbar -->
          <ul class="navbar-nav ml-auto">
              <!-- Authentication Links -->
              <?php if(auth()->guard()->guest()): ?>
                  <li class="nav-item">
                      <a class="nav-link" href="<?php echo e(route('login')); ?>"><?php echo e(__('Login')); ?></a>
                  </li>
                  <?php if(Route::has('register')): ?>
                      <li class="nav-item">
                          <a class="nav-link" href="<?php echo e(route('register')); ?>"><?php echo e(__('Register')); ?></a>
                      </li>
                  <?php endif; ?>
              <?php else: ?>

                <!-- touto doulefki -->
                <form id="logout-form" action="<?php echo e(route('logout')); ?>" method="POST" style="display: none;">
                 <?php echo csrf_field(); ?>
                </form>

                <li class="nav-item">
                  <a class="nav-link" href="/metarules">Meta-Rules</a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" href="/openhab-local">OpenHAB Local</a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" href="/openhab-cloud">OpenHAB Cloud</a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" href="/results">Results</a>
                </li>
                <?php if(Auth::user()->role == 'admin'): ?>
                <!--  <li class="nav-item">
                    <a class="nav-link" href="/users">Users</a>
                  </li> -->
                <?php endif; ?>
                <li class="nav-item">
                   <a class="nav-link" href="<?php echo e(route('logout')); ?>"
                   onclick="event.preventDefault();
                   document.getElementById('logout-form').submit();">
                      <?php echo e(__('Logout')); ?>

                   </a>
                </li>
              <?php endif; ?>
          </ul>

        </div>
    </div>
</nav>
<?php /**PATH /var/www/html/imcf/resources/views/inc/navbar.blade.php ENDPATH**/ ?>