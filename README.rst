NubiSave
========

With NubiSave you can store your data to several cloud providers simultaneously. And it is easy! Once set up, NubiSave acts like a local file system.
So all you need to do is open the nubisave directory with your favourite file browser or if you prefer from the command line.
To set up a NubiSave installation you can use our user friendly graphical user interface (GUI), or again if you prefer, you can set it up from the command line.

.. contents:: Table of Contents:

How it works
---------------

NubiSave will split every file you copy to the nubisave directory in an intelligent way, and distribute it to several cloud providers.
The individual file splits are uploaded asynchronously, so you do not need to wait long before you can write other files. 
If you read a file, for instance when opening a movie file from the nubisave directory, the files will be glued together again, without you noticing.
Normally, the file should be inside the cache; otherwise the file parts first need to be downloaded from the storage providers. 
But this should be fast, since they are downloaded concurrently. 

Security
----------------

NubiSave allows you to encrypt files you write to the nubisave directory transparently, which means automatically, without you having to do anything.
The files you write to the directory are encrypted, and the files you read are decrypted without you noticing.
Also, no cloud provider has access to all of your data, as it is distributed to several providers. So even if a provider gets to know your password,
they cannot read your data.

Durability and Availability
------------------------------------

Even though cloud providers promise high availability and no data loss, they can still fail for several hours, when you most need your data,
or you data may be lost due to a mistake.
NubiSave can increase durability and availability of data by automatically generating additional data for each file you write. 
The additional data is used in case one of the cloud storages is not accessible (due to server problems, or data loss), so you can 
still access your data, while everyone else has to wait.
Instead of duplicating your data, we use a well established method to generate this redundant data.
Compared to duplication, which means you need to store twice the amount of data, the additional amount of data is kept to a minimum.
For instance, if you use eight cloud providers, you may only need to store 1.14 times the original data. 
Moreover, you can increase this factor, so that you can even access your data after several cloud storage providers fail at the same time!


Vendor lock-in and Data Migration
--------------------------------------

What happens if one of your providers increases prices? Or another provider appears that is much cheaper? 
When you store all your data to one provider, it might become expensive to download all your data, and to upload it to a new provider.
Especially considering storage providers like Amazon S3, who charge for downloading data. Depending on your internet connection 
and the download speed offered by the provider, migration might take a long time. Also, when you use the API of the 
provider directly, you would have to rewrite your application.
That means you may be locked-in to a single provider.
NubiSave can prevent both scenarios from happening. First, only a part of your data is stored at each provider, so you only
need to migrate and pay for a fraction of the data. As you only need to download part of the data, this process is much faster.
Second, you do not need to rewrite your applications to work with the new provider, since you just access your files through the nubisave directory.

Performance and Cost
-----------------------

We uploaded a 15 GB Ubuntu home partition. We added about 30% redundancy to it, so we actually uploaded 20 GB of data in all.
The average throughput was 5 MB per seconds. We used five cloud storages; Dropbox, Sugarsync, Amazon S3, Google Storage and T-Online Mediacenter.
The 30% of additional data allowed us to tolerate two cloud storages failing simultaneously. But in our case, 
we choose to download data only from Dropbox, Sugarsync and T-Online, treating Amazon S3 and Google Storage, as if they were not available.
Since only Amazon S3 and Google Storage charge for downloading data, and the other providers don't, we could download our files
for free!








