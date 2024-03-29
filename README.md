# itracing2 

[Available on F-Droid](https://f-droid.org/en/packages/net.sylvek.itracing2/) ❤️

## History 📓
itracing2 is a free and open source application allowing to manage "iTag" devices.

Around 2015, [Bluetooth LE](https://www.link-labs.com/blog/bluetooth-vs-bluetooth-low-energy) _(or Bluetooth 4)_ became popular on smartphones and a lot of gadget-keyring-bluetooth-brands rised from nowhere _(think about smart things)_.
It was the begining of "IoT" for everyone and low cost devices from China invaded the market 😾 .

Because "no name company" are only interested by selling devices, software were incredibly unusable.

Customers and myself were so disappointated that i decided to create my own application.

In 2021, [Apple released his AirTags](https://en.wikipedia.org/wiki/AirTag) 😮 . That's pretty smart because those devices are pretty cheap and by using iPhone/iPad as sensors, AirTags can be found anywhere in the Earth. Yes, every AirTags have an unique device id 👍 and i suppose that Apple knows who is the owner of a dedicated AirTag.

## Do you BLE? ⚙️

If you want to learn more about BLE, there a lot of article on internet.
- [A Practical Guide to BLE Throughput](https://interrupt.memfault.com/blog/ble-throughput-primer)
- [List of Bluetooth profiles](https://en.wikipedia.org/wiki/List_of_Bluetooth_profiles)
- [List of Service Class IDs](https://docs.microsoft.com/en-us/windows/uwp/devices-sensors/aep-service-class-ids)

iTracing2 implementes ["Proximity Profile"](https://en.wikipedia.org/wiki/List_of_Bluetooth_profiles#Proximity_Profile_(PXP)) and should be compatible with a lot of devices.. **BUT** because [chinese iTag are so badly built](https://github.com/sylvek/itracing2/wiki/MLE-15), i hardcoded some stuff. _(prefer [Quintic PROXPR](https://github.com/sylvek/itracing2/wiki/Quintic-PROXR) chip if you can)_

## How that works?

[Read me on WIKI](https://github.com/sylvek/itracing2/wiki)

This application runs in background, so you do not have to launch it after each boot up.

Several actions are available :

* Capture your current position _(to work on Lineage OS, you need a Map application like OSMAnd or RMap)_
* Ringing your phone
* Vibrate your phone
* Call a custom URL (`GET` action)
* Call someone _(you need to explicitly give permission by navigating on permissions setting)_
* Play/Pause audio playlist
* And so one.. by [capturing custom event](https://github.com/sylvek/itracing2/wiki#custom-action) and plug [Tasker](https://github.com/sylvek/itracing2/wiki/Tasker)
