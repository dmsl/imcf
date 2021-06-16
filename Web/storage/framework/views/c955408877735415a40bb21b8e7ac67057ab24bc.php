

<?php $__env->startSection('content'); ?>
<div class="container">
      <div class="row justify-content-center">
          <div class="col-md-8">
            <a href="/metarules" class="btn btn-primary">Back to meta-rules</a>

            <br><br><br>
              <h1><u><?php echo e($metarule->name); ?></u>  </h1>

              <br>
              <div class="form-group">
                 <label>Time Duration:</label>
                 <input class="form-control" disabled value="<?php echo e($metarule->time); ?>">
              </div>
              <div class="form-group">
                 <label>Action:</label>
                 <input class="form-control" disabled value="Set <?php echo e($metarule->action); ?> level">
              </div>
              <div class="form-group">
                 <label>Value:</label>
                 <input class="form-control" disabled value="<?php echo e($metarule->value); ?>">
              </div>

              <hr>
              <small>Created on <?php echo e($metarule->created_at); ?>  </small>
              <hr>

              <a href="/metarules/<?php echo e($metarule->id); ?>/edit" class="btn btn-warning">Edit</a>

              <div class="float-right">
                <?php echo Form::open(['action' => ['MetarulesController@destroy', $metarule->id], 'method' => 'POST', 'class' => 'pull-right']); ?>

                  <?php echo e(Form::hidden('_method', 'DELETE')); ?>

                  <?php echo e(Form::submit('Delete', ['class' => 'btn btn-danger'] )); ?>

                <?php echo Form::close(); ?>

              </div>

    </div>
  </div>
</div>

<?php $__env->stopSection(); ?>

<?php echo $__env->make('layouts.app', \Illuminate\Support\Arr::except(get_defined_vars(), ['__data', '__path']))->render(); ?><?php /**PATH /var/www/html/imcf/resources/views/metarules/show.blade.php ENDPATH**/ ?>