  <?php $__env->startSection('content'); ?>
  <div class="container">
      <div class="row justify-content-center">
          <div class="col-md-8">
              <div class="card">
                  <div class="card-header"> </div>

                  <div class="card-body">
                      <?php if(session('status')): ?>
                          <div class="alert alert-success" role="alert">
                              <?php echo e(session('status')); ?>

                          </div>
                      <?php endif; ?>

                      <!---You are logged in!--->

                      <div class="panel-body">
                        <div>
                          <div class="float-md-left"><h1><strong>IMCF Results</strong></h1> </div>
                          <div class="float-md-right"><button type="button" class="btn btn-primary" onClick="window.location.reload();">Refresh Page</button> </div>
                        </div>
                        <br><br><br>

                        <table class="table table-striped">
                              <tr>
                                <th><h3><strong>Description</strong></h3></th>                                
                                <th><h3><strong>Value</strong></h3></th> 
                              </tr>
                              <tr>
                                <td><h4>Kilowatt hour (kWh):</h4></td>
                                <td><h4>9014 kWh</h4></td>
                              </tr>
                              <tr>
                                <td><h4>Total Average Error:</h4></td>
                                <td><h4>3.158%</h4></td>
                              </tr>                              
                              <tr>
                                <td><h4>Convenience Average Error:</h4></td>
                                <td><h4>3.158%</h4></td>
                              </tr>
                              <tr>
                                <td><h4>Budget Average Error:</h4></td>
                                <td><h4>3.158%</h4></td>
                              </tr>                              
                              <tr>
                                <td><h4>Standard Deviation:</h4></td>
                                <td><h4>3.158%</h4></td>
                              </tr>
                              
                          </table>

                        

                      </div>
                  </div>
              </div>
          </div>
      </div>
  </div>


  <?php $__env->stopSection(); ?>

<?php echo $__env->make('layouts.app', \Illuminate\Support\Arr::except(get_defined_vars(), ['__data', '__path']))->render(); ?><?php /**PATH /var/www/html/imcf/resources/views/energyplanner/index.blade.php ENDPATH**/ ?>