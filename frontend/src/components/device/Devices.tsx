import type {DeviceModel} from "../models/DeviceModel.ts";

type DevicesProps = {
    language: string;
    role: string;
    devices: DeviceModel[];
}

export default function Devices(props: Readonly<DevicesProps>){
    return(
        <>
            <h2>Devices</h2>
            <p>{props.devices.length}</p>
        </>
    )
}
