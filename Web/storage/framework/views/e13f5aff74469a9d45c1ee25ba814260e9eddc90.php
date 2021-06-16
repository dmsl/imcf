

  <?php $__env->startSection('content'); ?>
  <div class="container">
      <div class="row justify-content-center">
          <div class="col-md-8">
              <div class="card">
                  <div class="card-header">Meta-rules configuration</div>

                  <div class="card-body">
                      <?php if(session('status')): ?>
                          <div class="alert alert-success" role="alert">
                              <?php echo e(session('status')); ?>

                          </div>
                      <?php endif; ?>

                      <!---You are logged in!--->

                      <div class="panel-body">
                        <div>
                          <div class="float-md-left"><h1>Meta-Rules Table</h1> </div>
                          <div class="float-md-right"><a href="/metarules/create" class="btn btn-primary">Create meta-rule</a> </div>
                        </div>

                        <?php if(count($metarules)>0): ?>
                          <table class="table table-striped">
                              <tr>
                                <th>Description</th>
                                <th></th>
                                <th></th>
                              </tr>
                              <?php $__currentLoopData = $metarules; $__env->addLoop($__currentLoopData); foreach($__currentLoopData as $metarule): $__env->incrementLoopIndices(); $loop = $__env->getLastLoop(); ?>
                              <tr>
                                <td><a href="/metarules/<?php echo e($metarule->id); ?>"><?php echo e($metarule->name); ?></a></td>
                                <td><a href="/metarules/<?php echo e($metarule->id); ?>/edit" class="btn btn-warning">Edit</a></td>
                                <td>
                                  <?php echo Form::open(['action' => ['MetarulesController@destroy', $metarule->id], 'method' => 'POST', 'class' => 'pull-right']); ?>

                                    <?php echo e(Form::hidden('_method', 'DELETE')); ?>

                                    <?php echo e(Form::submit('Delete', ['class' => 'btn btn-danger'] )); ?>

                                  <?php echo Form::close(); ?>

                                </td>
                              </tr>
                              <?php endforeach; $__env->popLoop(); $loop = $__env->getLastLoop(); ?>
                          </table>
                        <?php else: ?>
                          <p>There are no meta-rules configured.</p>
                        <?php endif; ?>

                      </div>
                  </div>
              </div>
          </div>
      </div>
  </div>


  <?php $__env->stopSection(); ?>

<?php echo $__env->make('layouts.app', \Illuminate\Support\Arr::except(get_defined_vars(), ['__data', '__path']))->render(); ?><?php /**PATH /home/vagrant/Laravel/imcf/resources/views/metarules/index.blade.php ENDPATH**/ ?>